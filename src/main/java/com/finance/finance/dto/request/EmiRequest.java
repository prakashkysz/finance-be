package com.finance.finance.dto.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EmiRequest(

        @Schema(example = "500000", description = "Principal loan amount in rupees")
        @NotNull(message = "Loan amount is required")
        @DecimalMin(value = "1000", message = "Loan amount must be at least 1000")
        @DecimalMax(value = "100000000", message = "Loan amount must be at most 10 crore")
        @Digits(integer = 9, fraction = 2, message = "Loan amount can have at most 2 decimal places")
        BigDecimal loanAmount,

        @Schema(example = "11.5", description = "Annual interest rate in percent")
        @NotNull(message = "Annual interest rate is required")
        @DecimalMin(value = "0.0", message = "Interest rate cannot be negative")
        @DecimalMax(value = "50.0", message = "Interest rate must be at most 50%")
        @Digits(integer = 2, fraction = 2, message = "Interest rate can have at most 2 decimal places")
        BigDecimal annualInterestRate,

        @Schema(example = "36", description = "Loan tenure in months")
        @NotNull(message = "Tenure is required")
        @Min(value = 1, message = "Tenure must be at least 1 month")
        @Max(value = 480, message = "Tenure must be at most 480 months")
        Integer tenureMonths,

        @Schema(example = "false", description = "Include the month-by-month amortization schedule")
        Boolean includeSchedule) {
}
