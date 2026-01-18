package com.kapitalbank.payment.model.dto;

import jakarta.validation.constraints.NotBlank;

public record InstanceDto(
        @NotBlank String instanceId
) {
}
