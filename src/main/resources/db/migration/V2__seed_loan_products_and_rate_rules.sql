INSERT INTO loan_products (code, name, description, base_rate, min_amount, max_amount,
                           min_tenure_months, max_tenure_months, min_credit_score, processing_fee_percent)
VALUES ('PERSONAL', 'Personal Loan', 'Unsecured loan for personal needs such as travel, medical or wedding expenses',
        10.50, 50000.00, 2500000.00, 6, 60, 650, 2.00),
       ('HOME', 'Home Loan', 'Secured loan to buy, build or renovate a residential property',
        8.50, 500000.00, 50000000.00, 60, 360, 700, 0.50),
       ('VEHICLE', 'Vehicle Loan', 'Secured loan to buy a new or used two-wheeler or car',
        9.25, 100000.00, 5000000.00, 12, 84, 650, 1.00),
       ('EDUCATION', 'Education Loan', 'Loan for higher education fees in India or abroad',
        9.00, 50000.00, 7500000.00, 12, 180, 650, 0.75);

-- max_value NULL means "no upper limit"; ranges are inclusive on both ends
INSERT INTO interest_rate_rules (factor, min_value, max_value, adjustment, description)
VALUES ('LOAN_AMOUNT', 0.00, 100000.00, 2.00, 'Loan amount up to 1 lakh'),
       ('LOAN_AMOUNT', 100000.01, 500000.00, 1.00, 'Loan amount above 1 lakh up to 5 lakh'),
       ('LOAN_AMOUNT', 500000.01, 1000000.00, 0.00, 'Loan amount above 5 lakh up to 10 lakh'),
       ('LOAN_AMOUNT', 1000000.01, NULL, -0.50, 'Loan amount above 10 lakh'),

       ('TENURE', 1, 12, 0.00, 'Tenure up to 12 months'),
       ('TENURE', 13, 36, 0.50, 'Tenure from 13 to 36 months'),
       ('TENURE', 37, NULL, 1.00, 'Tenure above 36 months'),

       ('CREDIT_SCORE', 300, 649, 3.00, 'Credit score below 650 (poor)'),
       ('CREDIT_SCORE', 650, 699, 2.00, 'Credit score 650 to 699 (fair)'),
       ('CREDIT_SCORE', 700, 749, 0.00, 'Credit score 700 to 749 (good)'),
       ('CREDIT_SCORE', 750, 799, -1.00, 'Credit score 750 to 799 (very good)'),
       ('CREDIT_SCORE', 800, 900, -1.50, 'Credit score 800 and above (excellent)');
