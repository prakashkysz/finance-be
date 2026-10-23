package com.finance.finance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.finance.finance.dto.request.InterestRateRequest;
import com.finance.finance.dto.response.InterestRateResponse;
import com.finance.finance.entity.InterestRateRule;
import com.finance.finance.entity.LoanProduct;
import com.finance.finance.enums.RateFactor;
import com.finance.finance.exception.BadRequestException;
import com.finance.finance.repository.InterestRateRuleRepository;

class InterestRateServiceTest {

    private final LoanProductService loanProductService = mock(LoanProductService.class);
    private final InterestRateRuleRepository ruleRepository = mock(InterestRateRuleRepository.class);
    private final CreditScoreService creditScoreService = mock(CreditScoreService.class);
    private final InterestRateService service = new InterestRateService(loanProductService, ruleRepository,
            creditScoreService, new EmiService());

    @BeforeEach
    void setUp() {
        LoanProduct personal = LoanProduct.builder()
                .code("PERSONAL").name("Personal Loan")
                .baseRate(new BigDecimal("10.50"))
                .minAmount(new BigDecimal("50000")).maxAmount(new BigDecimal("2500000"))
                .minTenureMonths(6).maxTenureMonths(60)
                .minCreditScore(650)
                .processingFeePercent(new BigDecimal("2.00"))
                .active(true)
                .build();
        when(loanProductService.getActiveProduct("PERSONAL")).thenReturn(personal);
        when(ruleRepository.findByActiveTrueOrderByFactorAscMinValueAsc()).thenReturn(List.of(
                rule(RateFactor.LOAN_AMOUNT, "0", "100000", "2.00"),
                rule(RateFactor.LOAN_AMOUNT, "100000.01", "500000", "1.00"),
                rule(RateFactor.LOAN_AMOUNT, "500000.01", "1000000", "0.00"),
                rule(RateFactor.LOAN_AMOUNT, "1000000.01", null, "-0.50"),
                rule(RateFactor.TENURE, "1", "12", "0.00"),
                rule(RateFactor.TENURE, "13", "36", "0.50"),
                rule(RateFactor.TENURE, "37", null, "1.00"),
                rule(RateFactor.CREDIT_SCORE, "650", "699", "2.00"),
                rule(RateFactor.CREDIT_SCORE, "700", "749", "0.00"),
                rule(RateFactor.CREDIT_SCORE, "750", "799", "-1.00"),
                rule(RateFactor.CREDIT_SCORE, "800", "900", "-1.50")));
    }

    @Test
    void smallLoanWithGoodScoreGetsBaseRatePlusAdjustments() {
        InterestRateResponse response = service.calculate(
                new InterestRateRequest("PERSONAL", new BigDecimal("100000"), 24, 780), null);

        // 10.50 base + 2.00 amount + 0.50 tenure - 1.00 score
        assertThat(response.eligible()).isTrue();
        assertThat(response.finalAnnualRate()).isEqualByComparingTo("12.00");
        assertThat(response.adjustments()).hasSize(3);
        assertThat(response.processingFee()).isEqualByComparingTo("2000.00");
    }

    @Test
    void largeLoanWithFairScoreGetsHigherScorePremium() {
        InterestRateResponse response = service.calculate(
                new InterestRateRequest("PERSONAL", new BigDecimal("1000000"), 60, 680), null);

        // 10.50 base + 0.00 amount + 1.00 tenure + 2.00 score
        assertThat(response.finalAnnualRate()).isEqualByComparingTo("13.50");
    }

    @Test
    void scoreBelowProductMinimumIsNotEligible() {
        InterestRateResponse response = service.calculate(
                new InterestRateRequest("PERSONAL", new BigDecimal("100000"), 24, 600), null);

        assertThat(response.eligible()).isFalse();
        assertThat(response.finalAnnualRate()).isNull();
        assertThat(response.rejectionReason()).contains("650");
    }

    @Test
    void amountOutsideProductLimitsIsRejected() {
        assertThatThrownBy(() -> service.calculate(
                new InterestRateRequest("PERSONAL", new BigDecimal("10000"), 24, 780), null))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void missingScoreForAnonymousUserIsRejected() {
        assertThatThrownBy(() -> service.calculate(
                new InterestRateRequest("PERSONAL", new BigDecimal("100000"), 24, null), null))
                .isInstanceOf(BadRequestException.class);
    }

    private static InterestRateRule rule(RateFactor factor, String min, String max, String adjustment) {
        return InterestRateRule.builder()
                .factor(factor)
                .minValue(new BigDecimal(min))
                .maxValue(max == null ? null : new BigDecimal(max))
                .adjustment(new BigDecimal(adjustment))
                .description(factor + " " + min + "-" + max)
                .active(true)
                .build();
    }
}
