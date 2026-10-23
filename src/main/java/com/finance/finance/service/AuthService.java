package com.finance.finance.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finance.finance.config.SecurityProperties;
import com.finance.finance.dto.request.LoginRequest;
import com.finance.finance.dto.request.RefreshTokenRequest;
import com.finance.finance.dto.request.RegisterRequest;
import com.finance.finance.dto.response.AuthResponse;
import com.finance.finance.dto.response.UserResponse;
import com.finance.finance.entity.RefreshToken;
import com.finance.finance.entity.User;
import com.finance.finance.enums.Role;
import com.finance.finance.exception.AccountLockedException;
import com.finance.finance.exception.ApiException;
import com.finance.finance.exception.ConflictException;
import com.finance.finance.exception.UnauthorizedException;
import com.finance.finance.repository.RefreshTokenRepository;
import com.finance.finance.repository.UserRepository;
import com.finance.finance.security.JwtService;
import com.finance.finance.util.HashUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthService {

    private static final String INVALID_CREDENTIALS = "Invalid email or password";
    private static final int REFRESH_TOKEN_BYTES = 32;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SecurityProperties securityProperties;
    private final SecureRandom secureRandom = new SecureRandom();
    /** Compared against when the email is unknown, so response time does not reveal which emails are registered. */
    private final String dummyPasswordHash;

    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder, JwtService jwtService, SecurityProperties securityProperties) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.securityProperties = securityProperties;
        this.dummyPasswordHash = passwordEncoder.encode("dummy-password-for-timing-protection");
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("An account with this email already exists");
        }
        if (userRepository.existsByMobileNumber(request.mobileNumber())) {
            throw new ConflictException("An account with this mobile number already exists");
        }

        User user = User.builder()
                .email(email)
                .mobileNumber(request.mobileNumber())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.CUSTOMER)
                .build();
        userRepository.save(user);

        log.info("Registered new user id={}", user.getId());
        return UserResponse.from(user);
    }

    @Transactional(noRollbackFor = ApiException.class)
    public AuthResponse login(LoginRequest request) {
        Optional<User> existing = userRepository.findByEmail(normalizeEmail(request.email()));
        if (existing.isEmpty()) {
            passwordEncoder.matches(request.password(), dummyPasswordHash);
            throw new UnauthorizedException(INVALID_CREDENTIALS);
        }

        User user = existing.get();
        Instant now = Instant.now();
        if (user.isLockedAt(now)) {
            throw new AccountLockedException(user.getLockedUntil());
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            registerFailedAttempt(user, now);
            throw new UnauthorizedException(INVALID_CREDENTIALS);
        }
        if (!user.isEnabled()) {
            throw new UnauthorizedException("Account is disabled. Please contact support");
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(now);

        log.info("User id={} logged in", user.getId());
        return issueTokens(user);
    }

    @Transactional(noRollbackFor = ApiException.class)
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken stored = refreshTokenRepository.findByTokenHash(HashUtils.sha256Hex(request.refreshToken()))
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        User user = stored.getUser();

        if (stored.isRevoked()) {
            // A revoked token being replayed means it was likely stolen: end every session for this user
            int revoked = refreshTokenRepository.revokeAllActiveByUserId(user.getId());
            log.warn("Refresh token reuse detected for user id={}, revoked {} active token(s)", user.getId(), revoked);
            throw new UnauthorizedException("Refresh token has been revoked. Please log in again");
        }
        if (stored.isExpiredAt(Instant.now())) {
            throw new UnauthorizedException("Refresh token has expired. Please log in again");
        }
        if (!user.isEnabled()) {
            throw new UnauthorizedException("Account is disabled. Please contact support");
        }

        stored.setRevoked(true);
        return issueTokens(user);
    }

    @Transactional
    public void logout(Long userId, RefreshTokenRequest request) {
        refreshTokenRepository.findByTokenHash(HashUtils.sha256Hex(request.refreshToken()))
                .filter(token -> token.getUser().getId().equals(userId))
                .ifPresent(token -> token.setRevoked(true));
        log.info("User id={} logged out", userId);
    }

    private void registerFailedAttempt(User user, Instant now) {
        SecurityProperties.Login policy = securityProperties.login();
        int attempts = user.getFailedLoginAttempts() + 1;
        if (attempts >= policy.maxFailedAttempts()) {
            user.setLockedUntil(now.plus(policy.lockDuration()));
            user.setFailedLoginAttempts(0);
            log.warn("User id={} locked after {} failed login attempts", user.getId(), attempts);
        } else {
            user.setFailedLoginAttempts(attempts);
        }
    }

    private AuthResponse issueTokens(User user) {
        String rawRefreshToken = generateRefreshToken();
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .tokenHash(HashUtils.sha256Hex(rawRefreshToken))
                .expiresAt(Instant.now().plus(securityProperties.jwt().refreshTokenValidity()))
                .build());

        return new AuthResponse(jwtService.generateAccessToken(user), rawRefreshToken, "Bearer",
                jwtService.getAccessTokenValiditySeconds(), UserResponse.from(user));
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[REFRESH_TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
