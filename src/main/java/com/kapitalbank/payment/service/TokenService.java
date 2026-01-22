package com.kapitalbank.payment.service;


import com.kapitalbank.payment.dao.entity.Token;
import com.kapitalbank.payment.dao.entity.User;
import com.kapitalbank.payment.dao.repo.TokenRepository;
import com.kapitalbank.payment.model.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;

    public void saveToken(Token token) {
        tokenRepository.save(token);
    }

    public Token findTokenIfExistByUserId(Long userId) {
        Optional<Token> optionalToken = tokenRepository.findByUserId(userId);

        return optionalToken.orElse(null);
    }
    
    public void deleteById(Long id) {
        tokenRepository.deleteById(id);
    }

    public Token getToken(String activationToken) {
        return tokenRepository.findByActivationToken(activationToken)
                .orElseThrow(() -> new NotFoundException("Token not found"));
    }

    public void reSetActivationToken(User user, String activationToken) {
        Token existingToken = findTokenIfExistByUserId(user.getId());
        if (existingToken != null) {
            existingToken.setActivationToken(activationToken);
            saveToken(existingToken);
        } else {
            Token tokenEntity = Token.builder()
                    .activationToken(activationToken)
                    .userId(user.getId())
                    .createdAt(LocalDateTime.now())
                    .build();
            saveToken(tokenEntity);
        }
    }

}
