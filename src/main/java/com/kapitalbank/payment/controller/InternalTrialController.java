package com.kapitalbank.payment.controller;

import com.kapitalbank.payment.model.dto.InstanceDto;
import com.kapitalbank.payment.model.dto.TrialActivationResponse;
import com.kapitalbank.payment.service.TrialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/licence")
@Validated
public class InternalTrialController {

    private final TrialService trialService;

    @PostMapping("/check-trial")
    public TrialActivationResponse checkTrial(@RequestBody @Valid InstanceDto instanceId) {
        return trialService.checkTrial(instanceId);
    }
}
