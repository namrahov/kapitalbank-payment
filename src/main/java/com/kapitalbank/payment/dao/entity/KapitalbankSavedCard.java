package com.kapitalbank.payment.dao.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

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

    /**
     * Owner of the saved card
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Token returned by Kapitalbank (never store real PAN)
     */
    @Column(name = "stored_token_id", nullable = false, unique = true)
    private Long storedTokenId;

    /**
     * Masked card number (e.g. 4111********1111)
     */
    @Column(name = "card_mask", nullable = false, length = 50)
    private String cardMask;

    /**
     * VISA / MASTERCARD / etc.
     */
    @Column(name = "card_brand", length = 50)
    private String cardBrand;

    /**
     * Card expiration (MM/YY or MM/YYYY depending on bank format)
     */
    @Column(name = "expiration", length = 10)
    private String expiration;

    /**
     * Friendly name shown to user
     */
    @Column(name = "display_name", length = 100)
    private String displayName;

    /**
     * Default card flag
     */
    @Column(name = "is_default", nullable = false)
    private boolean defaultCard = false;

    /**
     * Soft-disable flag
     */
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

