package com.kapitalbank.payment.service;

import com.kapitalbank.payment.dao.entity.TrialEntity;
import com.kapitalbank.payment.dao.repo.TrialRepository;
import com.kapitalbank.payment.model.dto.InstanceDto;
import com.kapitalbank.payment.model.dto.TrialActivationResponse;
import com.kapitalbank.payment.model.exception.TrialException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TrialService {

    private final TrialRepository trialRepository;

    public TrialActivationResponse checkTrial(InstanceDto instanceDto) {
        String instanceId = instanceDto.instanceId();
            Optional<TrialEntity> existing = trialRepository.findByInstanceId(instanceId);

            if (existing.isPresent()) {
                throw  new TrialException("Trial is over");
            }

            Instant now = Instant.now();
            Instant until = now.plus(14, ChronoUnit.DAYS);

            // Persist that this fingerprint has USED its trial
            trialRepository.save(new TrialEntity(instanceId, now, until));

            return new TrialActivationResponse(now, until);
        }

}
