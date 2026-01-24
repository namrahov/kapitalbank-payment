package com.kapitalbank.payment.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegistrationDto {

    @Size(max = 80, message = "Email cannot exceed 80 characters.")
    private String email;

    @Size(min = 3, max = 80, message = "{password.length}")
    private String password;

    @NotBlank
    @Size(min = 5, max = 15, message = "{nickname.size}")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "{nickname.format}")
    private String username;

}
