package com.finance.finance.dto.response;

import java.math.BigDecimal;

import com.finance.finance.enums.RateFactor;

public record RateAdjustment(
        RateFactor factor,
        String description,
        BigDecimal adjustment) {
}
