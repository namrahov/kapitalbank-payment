package com.kapitalbank.payment.dao.repo;


import com.kapitalbank.payment.dao.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.stream.Stream;

public interface UserRepository extends JpaRepository<User, Long>,
        JpaSpecificationExecutor<User> {

    List<User> findByEmailOrUsername(String email, String username);

    Stream<User> findByEmail(String email);

    Stream<User> findByEmailAndIsActiveTrue(String username);

    Stream<User> findByIdAndIsActiveTrue(Long id);

    Stream<User> findUserById(Long id);
}
