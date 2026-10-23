package com.finance.finance.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.finance.finance.entity.CreditScoreReport;

public interface CreditScoreReportRepository extends JpaRepository<CreditScoreReport, Long> {

    Optional<CreditScoreReport> findFirstByUser_IdOrderByCheckedAtDesc(Long userId);

    List<CreditScoreReport> findByUser_IdOrderByCheckedAtDesc(Long userId);

    boolean existsByPanHashAndUser_IdNot(String panHash, Long userId);
}
