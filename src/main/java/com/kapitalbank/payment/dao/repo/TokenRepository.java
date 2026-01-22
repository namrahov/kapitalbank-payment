package com.kapitalbank.payment.dao.repo;

import com.kapitalbank.payment.dao.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {


    Optional<Token> findByUserId(Long id);

    Optional<Token> findByActivationToken(String activationToken);

}
