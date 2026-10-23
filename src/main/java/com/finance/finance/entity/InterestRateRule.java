package com.finance.finance.entity;

import java.math.BigDecimal;

import com.finance.finance.enums.RateFactor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "interest_rate_rules")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterestRateRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RateFactor factor;

    @Column(name = "min_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal minValue;

    /** {@code null} means the range has no upper limit. */
    @Column(name = "max_value", precision = 15, scale = 2)
    private BigDecimal maxValue;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal adjustment;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private boolean active;

    public boolean matches(BigDecimal value) {
        return value.compareTo(minValue) >= 0 && (maxValue == null || value.compareTo(maxValue) <= 0);
    }
}
