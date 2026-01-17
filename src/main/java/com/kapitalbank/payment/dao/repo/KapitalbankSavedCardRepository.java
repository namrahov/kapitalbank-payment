package com.kapitalbank.payment.dao.repo;

import com.kapitalbank.payment.dao.entity.KapitalbankSavedCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KapitalbankSavedCardRepository
        extends JpaRepository<KapitalbankSavedCard, Long> {

    List<KapitalbankSavedCard> findByActiveTrue();

    List<KapitalbankSavedCard> findByUserId(Long userId);

    List<KapitalbankSavedCard> findByUserIdAndActiveTrue(Long userId);

    Optional<KapitalbankSavedCard> findByUserIdAndDefaultCardTrue(Long userId);

    boolean existsByStoredTokenId(Long storedTokenId);
}
