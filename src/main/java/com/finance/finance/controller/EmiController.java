package com.finance.finance.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finance.finance.dto.request.EmiRequest;
import com.finance.finance.dto.response.EmiResponse;
import com.finance.finance.service.EmiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/emi")
@RequiredArgsConstructor
@Tag(name = "EMI Calculator", description = "Public EMI calculator using the reducing-balance method")
public class EmiController {

    private final EmiService emiService;

    @PostMapping("/calculate")
    @Operation(summary = "Calculate monthly EMI, total interest and optional amortization schedule")
    public EmiResponse calculate(@Valid @RequestBody EmiRequest request) {
        return emiService.calculate(request.loanAmount(), request.annualInterestRate(), request.tenureMonths(),
                Boolean.TRUE.equals(request.includeSchedule()));
    }
}
