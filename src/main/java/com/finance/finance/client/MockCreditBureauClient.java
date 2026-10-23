package com.finance.finance.client;

import org.springframework.stereotype.Component;

import com.finance.finance.util.HashUtils;

/** Returns a deterministic score between 300 and 900 per PAN, so the same PAN always gets the same score. */
@Component
public class MockCreditBureauClient implements CreditBureauClient {

    private static final int MIN_SCORE = 300;
    private static final int SCORE_RANGE = 601;

    @Override
    public int fetchCreditScore(String pan) {
        int seed = Integer.parseUnsignedInt(HashUtils.sha256Hex(pan).substring(0, 7), 16);
        return MIN_SCORE + seed % SCORE_RANGE;
    }

    @Override
    public String sourceName() {
        return "MOCK_BUREAU";
    }
}
