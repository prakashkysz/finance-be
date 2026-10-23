package com.finance.finance.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finance.finance.client.CreditBureauClient;
import com.finance.finance.config.CreditScoreProperties;
import com.finance.finance.config.SecurityProperties;
import com.finance.finance.dto.request.CreditScoreCheckRequest;
import com.finance.finance.dto.response.CreditScoreResponse;
import com.finance.finance.entity.CreditScoreReport;
import com.finance.finance.enums.CreditScoreBand;
import com.finance.finance.exception.BadRequestException;
import com.finance.finance.exception.ConflictException;
import com.finance.finance.repository.CreditScoreReportRepository;
import com.finance.finance.repository.UserRepository;
import com.finance.finance.util.HashUtils;
import com.finance.finance.util.MaskingUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditScoreService {

    private final CreditScoreReportRepository reportRepository;
    private final UserRepository userRepository;
    private final CreditBureauClient creditBureauClient;
    private final CreditScoreProperties creditScoreProperties;
    private final SecurityProperties securityProperties;

    @Transactional
    public CreditScoreResponse checkCreditScore(Long userId, CreditScoreCheckRequest request) {
        String pan = request.pan().trim().toUpperCase();
        String panHash = HashUtils.hmacSha256Hex(securityProperties.panHashSecret(), pan);

        if (reportRepository.existsByPanHashAndUser_IdNot(panHash, userId)) {
            throw new ConflictException("This PAN is already linked to another account");
        }

        Optional<CreditScoreReport> latest = reportRepository.findFirstByUser_IdOrderByCheckedAtDesc(userId);
        if (latest.isPresent()) {
            CreditScoreReport report = latest.get();
            if (!report.getPanHash().equals(panHash)) {
                throw new BadRequestException("PAN does not match the PAN linked to your account");
            }
            // Reusing a recent report avoids repeated bureau enquiries, which lower the customer's score
            Instant validUntil = report.getCheckedAt().plus(creditScoreProperties.reportValidity());
            if (validUntil.isAfter(Instant.now())) {
                return CreditScoreResponse.from(report, true);
            }
        }

        int score = creditBureauClient.fetchCreditScore(pan);
        CreditScoreReport report = reportRepository.save(CreditScoreReport.builder()
                .user(userRepository.getReferenceById(userId))
                .panHash(panHash)
                .panMasked(MaskingUtils.maskPan(pan))
                .score(score)
                .band(CreditScoreBand.fromScore(score))
                .source(creditBureauClient.sourceName())
                .checkedAt(Instant.now())
                .build());

        log.info("Fetched credit score for user id={} from {}", userId, report.getSource());
        return CreditScoreResponse.from(report, false);
    }

    @Transactional(readOnly = true)
    public List<CreditScoreResponse> getHistory(Long userId) {
        return reportRepository.findByUser_IdOrderByCheckedAtDesc(userId).stream()
                .map(report -> CreditScoreResponse.from(report, true))
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<Integer> findLatestScore(Long userId) {
        return reportRepository.findFirstByUser_IdOrderByCheckedAtDesc(userId).map(CreditScoreReport::getScore);
    }
}
