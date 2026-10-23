package com.finance.finance.enums;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CreditScoreBand {
    EXCELLENT(800, 900, "Excellent - eligible for the best interest rates"),
    VERY_GOOD(750, 799, "Very good - eligible for preferential interest rates"),
    GOOD(700, 749, "Good - eligible for standard interest rates"),
    FAIR(650, 699, "Fair - eligible with a higher interest rate"),
    POOR(300, 649, "Poor - most loan products will be declined");

    private final int minScore;
    private final int maxScore;
    private final String description;

    public static CreditScoreBand fromScore(int score) {
        return Arrays.stream(values())
                .filter(band -> score >= band.minScore && score <= band.maxScore)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Credit score out of range: " + score));
    }
}
