package com.kapitalbank.payment.dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "kapitalbank_payments",
        indexes = {
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_user", columnList = "user_id"),
                @Index(name = "idx_reference", columnList = "reference_type,reference_id")
        })
@Getter
@Setter
public class KapitalbankPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long orderId;

    private String password;
    private String secret;

    private BigDecimal amount;

    @Column(length = 3)
    private String currency = "AZN";

    private String status;
    private String type;

    private String approvalCode;
    private String transactionId;
    private String rrn;

    private String cardMask;
    private String cardBrand;

    private Long storedTokenId;
    private String description;

    private String language;

    private String errorCode;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private String referenceType;
    private Long referenceId;

    private Long userId;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> meta;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> response;

    private String ipAddress;

    @Column(columnDefinition = "TEXT")
    private String userAgent;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
