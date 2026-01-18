package com.kapitalbank.payment.dao.repo;

import com.kapitalbank.payment.dao.entity.TrialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrialRepository extends JpaRepository<TrialEntity, Long> {
    Optional<TrialEntity> findByInstanceId(String instanceId);
}
