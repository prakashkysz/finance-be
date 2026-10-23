package com.finance.finance.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finance.finance.dto.response.LoanProductResponse;
import com.finance.finance.service.LoanProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/loan-products")
@RequiredArgsConstructor
@Tag(name = "Loan Products", description = "Available loan products with their limits and base rates")
public class LoanProductController {

    private final LoanProductService loanProductService;

    @GetMapping
    @Operation(summary = "List all active loan products")
    public List<LoanProductResponse> getProducts() {
        return loanProductService.getActiveProducts();
    }

    @GetMapping("/{code}")
    @Operation(summary = "Get a loan product by code, e.g. PERSONAL")
    public LoanProductResponse getProduct(@PathVariable String code) {
        return loanProductService.getProduct(code);
    }
}
