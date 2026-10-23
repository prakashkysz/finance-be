package com.finance.finance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.finance.finance.entity.InterestRateRule;

public interface InterestRateRuleRepository extends JpaRepository<InterestRateRule, Long> {

    List<InterestRateRule> findByActiveTrueOrderByFactorAscMinValueAsc();
}
