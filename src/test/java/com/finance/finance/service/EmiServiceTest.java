package com.finance.finance.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.finance.finance.dto.response.EmiResponse;
import com.finance.finance.dto.response.EmiScheduleEntry;

class EmiServiceTest {

    private final EmiService emiService = new EmiService();

    @Test
    void calculatesReducingBalanceEmi() {
        EmiResponse response = emiService.calculate(new BigDecimal("100000"), new BigDecimal("12"), 12, true);

        assertThat(response.monthlyEmi()).isEqualByComparingTo("8884.88");
        assertThat(response.schedule()).hasSize(12);
        assertThat(response.totalPayable())
                .isEqualByComparingTo(response.loanAmount().add(response.totalInterest()));
    }

    @Test
    void scheduleClosesAtExactlyZero() {
        EmiResponse response = emiService.calculate(new BigDecimal("543210.55"), new BigDecimal("13.75"), 37, true);

        EmiScheduleEntry last = response.schedule().get(response.schedule().size() - 1);
        assertThat(last.closingBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        BigDecimal principalRepaid = response.schedule().stream()
                .map(EmiScheduleEntry::principalComponent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(principalRepaid).isEqualByComparingTo("543210.55");
    }

    @Test
    void zeroInterestSplitsPrincipalEvenly() {
        EmiResponse response = emiService.calculate(new BigDecimal("12000"), BigDecimal.ZERO, 12, false);

        assertThat(response.monthlyEmi()).isEqualByComparingTo("1000.00");
        assertThat(response.totalInterest()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.schedule()).isNull();
    }
}
