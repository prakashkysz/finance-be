package com.finance.finance.dto.response;

import java.time.Instant;

import com.finance.finance.entity.CreditScoreReport;
import com.finance.finance.enums.CreditScoreBand;

public record CreditScoreResponse(
        int score,
        CreditScoreBand band,
        String remarks,
        String panMasked,
        String source,
        Instant checkedAt,
        boolean cached) {

    public static CreditScoreResponse from(CreditScoreReport report, boolean cached) {
        return new CreditScoreResponse(report.getScore(), report.getBand(), report.getBand().getDescription(),
                report.getPanMasked(), report.getSource(), report.getCheckedAt(), cached);
    }
}
