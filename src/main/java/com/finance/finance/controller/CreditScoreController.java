package com.finance.finance.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finance.finance.config.OpenApiConfig;
import com.finance.finance.dto.request.CreditScoreCheckRequest;
import com.finance.finance.dto.response.CreditScoreResponse;
import com.finance.finance.security.UserPrincipal;
import com.finance.finance.service.CreditScoreService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/credit-score")
@RequiredArgsConstructor
@Tag(name = "Credit Score", description = "CIBIL-style credit score check (currently backed by a mock bureau)")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class CreditScoreController {

    private final CreditScoreService creditScoreService;

    @PostMapping("/check")
    @Operation(summary = "Check credit score by PAN",
            description = "The first PAN checked is linked to the account. A report is reused for 30 days "
                    + "before a fresh bureau enquiry is made.")
    public CreditScoreResponse check(@Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreditScoreCheckRequest request) {
        return creditScoreService.checkCreditScore(principal.getId(), request);
    }

    @GetMapping("/history")
    @Operation(summary = "List the logged-in user's credit score reports, newest first")
    public List<CreditScoreResponse> history(
            @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
        return creditScoreService.getHistory(principal.getId());
    }
}
