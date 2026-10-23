package com.finance.finance.dto.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InterestRateRequest(

        @Schema(example = "PERSONAL", description = "Loan product code, see GET /api/loan-products")
        @NotBlank(message = "Product code is required")
        String productCode,

        @Schema(example = "100000", description = "Requested loan amount in rupees")
        @NotNull(message = "Loan amount is required")
        @DecimalMin(value = "1000", message = "Loan amount must be at least 1000")
        @Digits(integer = 9, fraction = 2, message = "Loan amount can have at most 2 decimal places")
        BigDecimal loanAmount,

        @Schema(example = "24", description = "Loan tenure in months")
        @NotNull(message = "Tenure is required")
        @Min(value = 1, message = "Tenure must be at least 1 month")
        @Max(value = 480, message = "Tenure must be at most 480 months")
        Integer tenureMonths,

        @Schema(example = "780", description = "Optional. If omitted, the logged-in user's latest credit report is used")
        @Min(value = 300, message = "Credit score must be between 300 and 900")
        @Max(value = 900, message = "Credit score must be between 300 and 900")
        Integer creditScore) {
}
