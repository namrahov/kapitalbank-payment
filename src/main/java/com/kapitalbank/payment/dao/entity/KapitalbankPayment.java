package com.kapitalbank.payment.dao.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(
        name = "kapitalbank_payments",
        indexes = {
                @Index(name = "idx_kb_payment_status", columnList = "status"),
                @Index(name = "idx_kb_payment_user", columnList = "user_id"),
                @Index(name = "idx_kb_payment_reference", columnList = "reference_type,reference_id")
        }
)
public class KapitalbankPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    private String password;
    private String secret;

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    private String currency = "AZN";

    private String status;
    private String type;

    @Column(name = "approval_code")
    private String approvalCode;

    @Column(name = "transaction_id")
    private String transactionId;

    private String rrn;

    @Column(name = "card_mask")
    private String cardMask;

    @Column(name = "card_brand")
    private String cardBrand;

    @Column(name = "stored_token_id")
    private Long storedTokenId;

    private String description;
    private String language;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "reference_type")
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "user_id")
    private Long userId;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> meta;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> response;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "stored_token_id",
            referencedColumnName = "stored_token_id",
            insertable = false,
            updatable = false
    )
    private KapitalbankSavedCard savedCard;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    /* =======================
       Domain helpers
       ======================= */

    @Transient
    public boolean isSuccessful() {
        return status != null &&
                (status.equals("FullyPaid")
                        || status.equals("PartiallyPaid")
                        || status.equals("Approved"));
    }

    @Transient
    public boolean isPending() {
        return "Preparing".equals(status);
    }
}
