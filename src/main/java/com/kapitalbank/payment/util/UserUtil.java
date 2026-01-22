package com.kapitalbank.payment.util;


import com.kapitalbank.payment.dao.entity.Role;
import com.kapitalbank.payment.dao.entity.User;
import com.kapitalbank.payment.dao.repo.RoleRepository;
import com.kapitalbank.payment.dao.repo.UserRepository;
import com.kapitalbank.payment.model.dto.UserRegistrationDto;
import com.kapitalbank.payment.model.exception.NotFoundException;
import com.kapitalbank.payment.model.exception.UserException;
import com.kapitalbank.payment.service.TokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class UserUtil {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional(readOnly = true)
    public User findActiveUserByEmail(String email) {
        return userRepository.findByEmailAndIsActiveTrue(email)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("User not found"));
    }


    public List<User> findUserByEmailOrUsername(String email, String username) {
        return userRepository.findByEmailOrUsername(email, username);
    }

    @Transactional(readOnly = true)
    public User getCurrentUser(HttpServletRequest request) {
        if (request.getHeader("Authorization") == null) throw new UserException("User has not logged in");
        String token = request.getHeader("Authorization").substring(7);
        String email = jwtUtil.extractEmail(token);

        return findActiveUserByEmail(email);
    }

    public User buildUser(UserRegistrationDto dto, boolean isActive) {
        Role role = roleRepository.findById(1L).orElseThrow();

        return User.builder()
                .email(dto.getEmail())
                .username(dto.getUsername())
                .roles(Collections.singletonList(role))
                .password(bCryptPasswordEncoder.encode(dto.getPassword()))
                .build();
    }

    @Transactional(readOnly = true)
    public User findUserById(Long id) {
        return userRepository.findUserById(id)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public void checkIsCurrentUser(User currentUser, User user) {
        if (!Objects.equals(currentUser.getId(), user.getId())) throw new UserException("Can not change to other user");
    }



    @Transactional(readOnly = true)
    public List<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email).toList();
    }


    public String generateRandomPassword() {
        Random random = new Random();
        int randomPassword = 100000 + random.nextInt(900000);

        return String.valueOf(randomPassword);
    }

}