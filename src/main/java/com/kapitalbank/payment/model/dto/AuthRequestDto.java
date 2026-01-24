package com.kapitalbank.payment.model.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequestDto {

    @Size(max = 80, message = "emailOrNickname cannot exceed 80 characters.")
    private String emailOrNickname;
    private String password;

}
