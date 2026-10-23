package com.finance.finance.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreditScoreCheckRequest(

        @Schema(example = "ABCDE1234F", description = "10-character PAN")
        @NotBlank(message = "PAN is required")
        @Pattern(regexp = "^(?i)[A-Z]{5}[0-9]{4}[A-Z]$", message = "PAN must be in the format ABCDE1234F")
        String pan,

        @Schema(example = "true", description = "Customer consent to fetch the credit report from the bureau")
        @NotNull(message = "Consent is required")
        @AssertTrue(message = "You must consent to the credit bureau enquiry")
        Boolean consent) {
}
