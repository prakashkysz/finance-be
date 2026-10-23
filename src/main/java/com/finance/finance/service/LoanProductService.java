package com.finance.finance.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finance.finance.dto.response.LoanProductResponse;
import com.finance.finance.entity.LoanProduct;
import com.finance.finance.exception.ResourceNotFoundException;
import com.finance.finance.repository.LoanProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoanProductService {

    private final LoanProductRepository loanProductRepository;

    @Transactional(readOnly = true)
    public List<LoanProductResponse> getActiveProducts() {
        return loanProductRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(LoanProductResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public LoanProductResponse getProduct(String code) {
        return LoanProductResponse.from(getActiveProduct(code));
    }

    @Transactional(readOnly = true)
    public LoanProduct getActiveProduct(String code) {
        return loanProductRepository.findByCodeIgnoreCaseAndActiveTrue(code.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Loan product '" + code + "' not found"));
    }
}
