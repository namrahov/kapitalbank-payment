package com.kapitalbank.payment.dao.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.YearMonth;

@Getter
@Setter
@Entity
@Table(
        name = "kapitalbank_saved_cards",
        indexes = {
                @Index(name = "idx_kb_saved_cards_user", columnList = "user_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_kb_saved_cards_token",
                        columnNames = "stored_token_id"
                )
        }
)
public class KapitalbankSavedCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "stored_token_id", nullable = false, unique = true)
    private Long storedTokenId;

    @Column(name = "card_mask", nullable = false, length = 50)
    private String cardMask;

    @Column(name = "card_brand", length = 50)
    private String cardBrand;

    /**
     * MMYY (e.g. 0527)
     */
    @Column(name = "expiration", length = 4)
    private String expiration;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "is_default", nullable = false)
    private boolean defaultCard = false;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    /* =======================
       Derived properties
       ======================= */

    /**
     * Last 4 digits of card
     */
    @Transient
    public String getLastFour() {
        if (cardMask == null || cardMask.length() < 4) {
            return "";
        }
        return cardMask.substring(cardMask.length() - 4);
    }

    /**
     * **** **** **** 1234
     */
    @Transient
    public String getFormattedMask() {
        return "**** **** **** " + getLastFour();
    }

    /**
     * Expiration check
     */
    @Transient
    public boolean isExpired() {
        if (expiration == null || expiration.length() != 4) {
            return false;
        }

        int month = Integer.parseInt(expiration.substring(0, 2));
        int year = 2000 + Integer.parseInt(expiration.substring(2, 4));

        YearMonth expiry = YearMonth.of(year, month);
        return expiry.atEndOfMonth().isBefore(java.time.LocalDate.now());
    }
}
