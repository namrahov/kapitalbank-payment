package com.kapitalbank.payment.config;

import com.kapitalbank.payment.model.exception.ExceptionResponse;
import com.kapitalbank.payment.model.exception.TrialException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
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

}
