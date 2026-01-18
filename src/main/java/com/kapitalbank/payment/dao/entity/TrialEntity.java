package com.kapitalbank.payment.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "trials")
@NoArgsConstructor
public class TrialEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="instance_id", nullable = false, length = 128)
    private String instanceId;

    @Column(name="valid_from", nullable = false)
    private Instant validFrom;

    @Column(name="valid_until", nullable = false)
    private Instant validUntil;

    @CreationTimestamp
    @Column(name="created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name="updated_at", nullable = false)
    private Instant updatedAt;

    public TrialEntity(String instanceId, Instant validFrom, Instant validUntil) {
        this.instanceId = instanceId;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
    }
}
