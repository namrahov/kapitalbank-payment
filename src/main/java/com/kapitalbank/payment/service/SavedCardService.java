package com.kapitalbank.payment.service;

import com.kapitalbank.payment.dao.entity.KapitalbankSavedCard;
import com.kapitalbank.payment.dao.repo.KapitalbankSavedCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SavedCardService {

    private final KapitalbankSavedCardRepository repository;

    public void makeDefault(Long cardId, Long userId) {

        // unset others
        repository.findByUserId(userId)
                .forEach(card -> {
                    if (card.isDefaultCard()) {
                        card.setDefaultCard(false);
                    }
                });

        // set selected
        KapitalbankSavedCard card =
                repository.findById(cardId)
                        .orElseThrow(() ->
                                new IllegalArgumentException("Card not found"));

        if (!card.getUserId().equals(userId)) {
            throw new SecurityException("Card does not belong to user");
        }

        card.setDefaultCard(true);
    }
}

