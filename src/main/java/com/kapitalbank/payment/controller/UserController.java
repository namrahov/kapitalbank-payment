package com.kapitalbank.payment.controller;

import com.kapitalbank.payment.model.dto.AuthRequestDto;
import com.kapitalbank.payment.model.dto.ChangePasswordDto;
import com.kapitalbank.payment.model.dto.ForgetPasswordDto;
import com.kapitalbank.payment.model.dto.GeneralResponseDto;
import com.kapitalbank.payment.model.dto.UserRegistrationDto;
import com.kapitalbank.payment.model.dto.UserResponseDto;
import com.kapitalbank.payment.model.exception.RateLimitExceededException;
import com.kapitalbank.payment.service.UserService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users")
@Validated
public class UserController {

    private final UserService userService;
    private final Bucket activateAccountBucket;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GeneralResponseDto registerUser(@RequestBody @Valid UserRegistrationDto dto) {
        userService.registerUser(dto);
        return GeneralResponseDto.builder().message("Successfully registered").build();
    }

    @GetMapping("/active")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public GeneralResponseDto activateAccount(@RequestParam String token) {
        if (activateAccountBucket.tryConsume(1)) {
            userService.activateAccount(token);
            return GeneralResponseDto.builder().message("SUCCESS").build();
        } else {
            throw new RateLimitExceededException("TRY_AFTER_AN_HOUR");
        }
    }

    @PostMapping("/login")
    public String authenticate(@RequestBody @Valid AuthRequestDto dto) {
        return userService.authenticate(dto);
    }

    @GetMapping
    public UserResponseDto getUser(HttpServletRequest request) {
        return userService.getUser(request);
    }


    @PostMapping("/forget-password")
    public GeneralResponseDto forgetPassword(@RequestBody @Valid ForgetPasswordDto dto) {
        userService.forgetPassword(dto);
        return GeneralResponseDto.builder().message("SUCCESS").build();
    }

    @PostMapping("/change-password")
    public GeneralResponseDto updatePassword(@RequestParam String token,
                                             @RequestBody @Valid ChangePasswordDto dto) {
        userService.updatePassword(token, dto);
        return GeneralResponseDto.builder().message("SUCCESS").build();
    }

}
