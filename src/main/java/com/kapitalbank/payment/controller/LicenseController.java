package com.kapitalbank.payment.controller;

import com.kapitalbank.payment.service.LicenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/license")
@Validated
public class LicenseController {

    private final LicenseService licenseService;

    @GetMapping
    public String generateLicense(@RequestParam String instanceId) {
        return licenseService.generateLicense(instanceId);
    }
}
