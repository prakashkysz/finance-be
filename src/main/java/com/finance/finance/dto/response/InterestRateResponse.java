package com.finance.finance.dto.response;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.finance.finance.enums.CreditScoreBand;

import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record InterestRateResponse(
        String productCode,
        String productName,
        BigDecimal loanAmount,
        int tenureMonths,
        int creditScore,
        String creditScoreSource,
        CreditScoreBand creditScoreBand,
        boolean eligible,
        String rejectionReason,
        BigDecimal baseRate,
        List<RateAdjustment> adjustments,
        BigDecimal finalAnnualRate,
        BigDecimal monthlyEmi,
        BigDecimal totalInterest,
        BigDecimal processingFee,
        BigDecimal totalPayable) {
}
