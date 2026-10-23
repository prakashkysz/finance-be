package com.finance.finance.dto.response;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmiResponse(
        BigDecimal loanAmount,
        BigDecimal annualInterestRate,
        int tenureMonths,
        BigDecimal monthlyEmi,
        BigDecimal totalInterest,
        BigDecimal totalPayable,
        List<EmiScheduleEntry> schedule) {
}
