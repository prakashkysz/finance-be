package com.finance.finance.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finance.finance.dto.request.InterestRateRequest;
import com.finance.finance.dto.response.EmiResponse;
import com.finance.finance.dto.response.InterestRateResponse;
import com.finance.finance.dto.response.InterestRateRuleResponse;
import com.finance.finance.dto.response.RateAdjustment;
import com.finance.finance.entity.InterestRateRule;
import com.finance.finance.entity.LoanProduct;
import com.finance.finance.enums.CreditScoreBand;
import com.finance.finance.enums.RateFactor;
import com.finance.finance.exception.BadRequestException;
import com.finance.finance.repository.InterestRateRuleRepository;

import lombok.RequiredArgsConstructor;

/** Final rate = product base rate + amount adjustment + tenure adjustment + credit score adjustment. */
@Service
@RequiredArgsConstructor
public class InterestRateService {

    private static final String SCORE_SOURCE_PROVIDED = "PROVIDED";
    private static final String SCORE_SOURCE_REPORT = "CREDIT_REPORT";
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final LoanProductService loanProductService;
    private final InterestRateRuleRepository ruleRepository;
    private final CreditScoreService creditScoreService;
    private final EmiService emiService;

    @Transactional(readOnly = true)
    public InterestRateResponse calculate(InterestRateRequest request, Long currentUserId) {
        LoanProduct product = loanProductService.getActiveProduct(request.productCode());
        validateAgainstProduct(product, request);

        int creditScore;
        String scoreSource;
        if (request.creditScore() != null) {
            creditScore = request.creditScore();
            scoreSource = SCORE_SOURCE_PROVIDED;
        } else {
            creditScore = resolveScoreFromReport(currentUserId);
            scoreSource = SCORE_SOURCE_REPORT;
        }

        InterestRateResponse.InterestRateResponseBuilder response = InterestRateResponse.builder()
                .productCode(product.getCode())
                .productName(product.getName())
                .loanAmount(request.loanAmount().setScale(2, RoundingMode.HALF_UP))
                .tenureMonths(request.tenureMonths())
                .creditScore(creditScore)
                .creditScoreSource(scoreSource)
                .creditScoreBand(CreditScoreBand.fromScore(creditScore))
                .baseRate(product.getBaseRate());

        if (creditScore < product.getMinCreditScore()) {
            return response
                    .eligible(false)
                    .rejectionReason("A minimum credit score of " + product.getMinCreditScore()
                            + " is required for " + product.getName())
                    .build();
        }

        List<InterestRateRule> rules = ruleRepository.findByActiveTrueOrderByFactorAscMinValueAsc();
        List<RateAdjustment> adjustments = new ArrayList<>();
        addAdjustment(adjustments, rules, RateFactor.LOAN_AMOUNT, request.loanAmount());
        addAdjustment(adjustments, rules, RateFactor.TENURE, BigDecimal.valueOf(request.tenureMonths()));
        addAdjustment(adjustments, rules, RateFactor.CREDIT_SCORE, BigDecimal.valueOf(creditScore));

        BigDecimal finalRate = adjustments.stream()
                .map(RateAdjustment::adjustment)
                .reduce(product.getBaseRate(), BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        EmiResponse emi = emiService.calculate(request.loanAmount(), finalRate, request.tenureMonths(), false);
        BigDecimal processingFee = request.loanAmount()
                .multiply(product.getProcessingFeePercent())
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);

        return response
                .eligible(true)
                .adjustments(adjustments)
                .finalAnnualRate(finalRate)
                .monthlyEmi(emi.monthlyEmi())
                .totalInterest(emi.totalInterest())
                .processingFee(processingFee)
                .totalPayable(emi.totalPayable().add(processingFee))
                .build();
    }

    @Transactional(readOnly = true)
    public List<InterestRateRuleResponse> getActiveRules() {
        return ruleRepository.findByActiveTrueOrderByFactorAscMinValueAsc().stream()
                .map(InterestRateRuleResponse::from)
                .toList();
    }

    private void validateAgainstProduct(LoanProduct product, InterestRateRequest request) {
        if (request.loanAmount().compareTo(product.getMinAmount()) < 0
                || request.loanAmount().compareTo(product.getMaxAmount()) > 0) {
            throw new BadRequestException("Loan amount for " + product.getName() + " must be between "
                    + product.getMinAmount().toPlainString() + " and " + product.getMaxAmount().toPlainString());
        }
        if (request.tenureMonths() < product.getMinTenureMonths()
                || request.tenureMonths() > product.getMaxTenureMonths()) {
            throw new BadRequestException("Tenure for " + product.getName() + " must be between "
                    + product.getMinTenureMonths() + " and " + product.getMaxTenureMonths() + " months");
        }
    }

    private int resolveScoreFromReport(Long currentUserId) {
        if (currentUserId == null) {
            throw new BadRequestException(
                    "Credit score is required. Provide creditScore, or log in and check your credit score first");
        }
        return creditScoreService.findLatestScore(currentUserId)
                .orElseThrow(() -> new BadRequestException(
                        "No credit report found. Check your credit score first or provide creditScore"));
    }

    private static void addAdjustment(List<RateAdjustment> adjustments, List<InterestRateRule> rules,
            RateFactor factor, BigDecimal value) {
        rules.stream()
                .filter(rule -> rule.getFactor() == factor && rule.matches(value))
                .findFirst()
                .ifPresent(rule -> adjustments.add(
                        new RateAdjustment(factor, rule.getDescription(), rule.getAdjustment())));
    }
}
