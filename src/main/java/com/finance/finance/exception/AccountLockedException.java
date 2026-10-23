package com.finance.finance.exception;

import java.time.Duration;
import java.time.Instant;

import org.springframework.http.HttpStatus;

public class AccountLockedException extends ApiException {

    public AccountLockedException(Instant lockedUntil) {
        super(HttpStatus.LOCKED, buildMessage(lockedUntil));
    }

    private static String buildMessage(Instant lockedUntil) {
        long minutes = Math.max(1, Duration.between(Instant.now(), lockedUntil).toMinutes() + 1);
        return "Account is temporarily locked due to multiple failed login attempts. Try again in "
                + minutes + " minute(s)";
    }
}
