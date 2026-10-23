package com.finance.finance.client;

/** Abstraction over a credit bureau (CIBIL, Experian, Equifax, CRIF) so the real integration can replace the mock. */
public interface CreditBureauClient {

    int fetchCreditScore(String pan);

    String sourceName();
}
