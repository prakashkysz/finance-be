package com.finance.finance.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finance.finance.dto.request.InterestRateRequest;
import com.finance.finance.dto.response.InterestRateResponse;
import com.finance.finance.dto.response.InterestRateRuleResponse;
import com.finance.finance.security.UserPrincipal;
import com.finance.finance.service.InterestRateService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/interest-rate")
@RequiredArgsConstructor
@Tag(name = "Interest Rate Calculator",
        description = "Rate = product base rate + loan amount, tenure and credit score adjustments")
public class InterestRateController {

    private final InterestRateService interestRateService;

    @PostMapping("/calculate")
    @Operation(summary = "Calculate the personalised interest rate, EMI and processing fee",
            description = "Public. If creditScore is omitted and a bearer token is sent, "
                    + "the user's latest credit report is used.")
    public InterestRateResponse calculate(@Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody InterestRateRequest request) {
        return interestRateService.calculate(request, principal != null ? principal.getId() : null);
    }

    @GetMapping("/rules")
    @Operation(summary = "List the active interest rate rules")
    public List<InterestRateRuleResponse> rules() {
        return interestRateService.getActiveRules();
    }
}
