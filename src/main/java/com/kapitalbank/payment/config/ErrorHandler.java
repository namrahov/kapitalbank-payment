package com.kapitalbank.payment.config;

import com.kapitalbank.payment.model.exception.ExceptionResponse;
import com.kapitalbank.payment.model.exception.NotFoundException;
import com.kapitalbank.payment.model.exception.RateLimitExceededException;
import com.kapitalbank.payment.model.exception.TrialException;
import com.kapitalbank.payment.model.exception.UnavailableException;
import com.kapitalbank.payment.model.exception.UserException;
import com.kapitalbank.payment.model.exception.UserRegisterException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@RequiredArgsConstructor
public class ErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(TrialException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ExceptionResponse handleLicenseException(TrialException ex) {
        return new ExceptionResponse("TRIAL_OVER", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse handle(Exception ex) {
        return new ExceptionResponse("UNEXPECTED_EXCEPTION", ex.getMessage());
    }

    @ExceptionHandler(UnavailableException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ExceptionResponse handle(UnavailableException ex) {
        return new ExceptionResponse("UNAVAILABLE", ex.getMessage());
    }

    @ExceptionHandler(UserException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ExceptionResponse handle(UserException ex) {
        return new ExceptionResponse("USER_FAIL", ex.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionResponse handle(NotFoundException ex) {
        return new ExceptionResponse("NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handle(ConstraintViolationException ex) {
        return new ExceptionResponse("WRONG_INPUT", ex.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        // Retrieve the first field error, if available
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("Validation failed");

        // Create an exception response with a single error message
        ExceptionResponse response = new ExceptionResponse(
                "Validation Failed",
                errorMessage
        );

        return new ResponseEntity<>(response, headers, status);
    }

    @ExceptionHandler(UserRegisterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handle(UserRegisterException ex) {
        return new ExceptionResponse("REGISTRATION_FAIL", ex.getMessage());
    }


    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handle(BadCredentialsException ex) {
        return new ExceptionResponse(ex.getMessage(), "userNotFound");
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handle() {
        return new ExceptionResponse("Bad credentials", "The username or password provided is incorrect.");
    }

    @ExceptionHandler(RateLimitExceededException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ExceptionResponse handle(RateLimitExceededException ex) {
        return new ExceptionResponse("EXCEED_REQUEST_COUNT", ex.getMessage());
    }


}
