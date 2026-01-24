package com.kapitalbank.payment.service;

import com.kapitalbank.payment.model.dto.LicensePayload;
import com.kapitalbank.payment.util.LicenseEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class LicenseService {

    private final LicenseGeneratorService licenseGeneratorService;

    public String generateLicense(String instanceId) {
        LicensePayload payload = new LicensePayload();
        payload.setInstanceId(instanceId);
        payload.setValidUntil(Instant.now().plus(365, ChronoUnit.DAYS));

        return licenseGeneratorService.generate(payload);
    }

}
