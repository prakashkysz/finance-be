package com.finance.finance.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.finance.finance.dto.response.EmiResponse;
import com.finance.finance.dto.response.EmiScheduleEntry;

/** Reducing-balance EMI: EMI = P * r * (1 + r)^n / ((1 + r)^n - 1), with r the monthly rate. */
@Service
public class EmiService {

    private static final MathContext MC = MathContext.DECIMAL128;
    private static final BigDecimal MONTHS_TIMES_PERCENT = BigDecimal.valueOf(1200);
    private static final int MONEY_SCALE = 2;

    public EmiResponse calculate(BigDecimal principal, BigDecimal annualRate, int tenureMonths,
            boolean includeSchedule) {
        BigDecimal monthlyRate = annualRate.divide(MONTHS_TIMES_PERCENT, MC);
        BigDecimal emi = monthlyEmi(principal, monthlyRate, tenureMonths);

        List<EmiScheduleEntry> schedule = new ArrayList<>(tenureMonths);
        BigDecimal balance = principal.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal totalInterest = BigDecimal.ZERO;

        for (int month = 1; month <= tenureMonths; month++) {
            BigDecimal interest = balance.multiply(monthlyRate, MC).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
            boolean lastInstallment = month == tenureMonths;
            // The last installment absorbs rounding differences so the loan closes at exactly zero
            BigDecimal principalPart = lastInstallment ? balance : emi.subtract(interest);
            BigDecimal installment = lastInstallment ? balance.add(interest) : emi;
            BigDecimal closingBalance = balance.subtract(principalPart);

            schedule.add(new EmiScheduleEntry(month, balance, installment, principalPart, interest, closingBalance));
            totalInterest = totalInterest.add(interest);
            balance = closingBalance;
        }

        return new EmiResponse(
                principal.setScale(MONEY_SCALE, RoundingMode.HALF_UP),
                annualRate,
                tenureMonths,
                emi,
                totalInterest,
                principal.add(totalInterest).setScale(MONEY_SCALE, RoundingMode.HALF_UP),
                includeSchedule ? schedule : null);
    }

    private BigDecimal monthlyEmi(BigDecimal principal, BigDecimal monthlyRate, int tenureMonths) {
        if (monthlyRate.signum() == 0) {
            return principal.divide(BigDecimal.valueOf(tenureMonths), MONEY_SCALE, RoundingMode.HALF_UP);
        }
        BigDecimal growth = BigDecimal.ONE.add(monthlyRate).pow(tenureMonths, MC);
        return principal.multiply(monthlyRate, MC)
                .multiply(growth, MC)
                .divide(growth.subtract(BigDecimal.ONE), MC)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
