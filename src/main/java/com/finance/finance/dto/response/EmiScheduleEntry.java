package com.finance.finance.dto.response;

import java.math.BigDecimal;

public record EmiScheduleEntry(
        int installmentNumber,
        BigDecimal openingBalance,
        BigDecimal emi,
        BigDecimal principalComponent,
        BigDecimal interestComponent,
        BigDecimal closingBalance) {
}
