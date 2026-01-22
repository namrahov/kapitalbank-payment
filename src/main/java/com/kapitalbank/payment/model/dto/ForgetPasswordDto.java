package com.kapitalbank.payment.model.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgetPasswordDto {

    @Size(max = 80, message = "Email cannot exceed 80 characters.")
    private String email;

}
