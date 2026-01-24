package com.kapitalbank.payment.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;


@NoArgsConstructor
@Setter
@Getter
public class LicensePayload {
    private String instanceId;
    private Instant validUntil;
}
