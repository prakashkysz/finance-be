package com.finance.finance.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "app.credit-score")
public record CreditScoreProperties(@NotNull Duration reportValidity) {
}
