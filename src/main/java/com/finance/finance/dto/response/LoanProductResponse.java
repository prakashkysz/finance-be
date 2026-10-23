package com.finance.finance.dto.response;

import java.math.BigDecimal;

import com.finance.finance.entity.LoanProduct;

public record LoanProductResponse(
        String code,
        String name,
        String description,
        BigDecimal baseRate,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        int minTenureMonths,
        int maxTenureMonths,
        int minCreditScore,
        BigDecimal processingFeePercent) {

    public static LoanProductResponse from(LoanProduct product) {
        return new LoanProductResponse(product.getCode(), product.getName(), product.getDescription(),
                product.getBaseRate(), product.getMinAmount(), product.getMaxAmount(), product.getMinTenureMonths(),
                product.getMaxTenureMonths(), product.getMinCreditScore(), product.getProcessingFeePercent());
    }
}
