package com.finance.finance.dto.response;

import java.math.BigDecimal;

import com.finance.finance.entity.InterestRateRule;
import com.finance.finance.enums.RateFactor;

public record InterestRateRuleResponse(
        RateFactor factor,
        BigDecimal minValue,
        BigDecimal maxValue,
        BigDecimal adjustment,
        String description) {

    public static InterestRateRuleResponse from(InterestRateRule rule) {
        return new InterestRateRuleResponse(rule.getFactor(), rule.getMinValue(), rule.getMaxValue(),
                rule.getAdjustment(), rule.getDescription());
    }
}
