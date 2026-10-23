package com.finance.finance.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.finance.finance.entity.LoanProduct;

public interface LoanProductRepository extends JpaRepository<LoanProduct, Long> {

    List<LoanProduct> findByActiveTrueOrderByNameAsc();

    Optional<LoanProduct> findByCodeIgnoreCaseAndActiveTrue(String code);
}
