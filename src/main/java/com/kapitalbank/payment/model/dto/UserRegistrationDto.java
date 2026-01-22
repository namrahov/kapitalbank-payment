package com.kapitalbank.payment.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegistrationDto {

    @Size(max = 80, message = "Email cannot exceed 80 characters.")
    private String email;

    @Size(min = 8, max = 80, message = "{password.length}")
    private String password;

    @NotBlank
    @Size(min = 5, max = 25, message = "{fullName.size}")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "{fullName.content}")
    @Pattern(regexp = "^\\S+\\s+\\S+(\\s+\\S+)*$", message = "{fullName.parts}")
    private String fullName;

    @NotBlank
    @Size(min = 5, max = 15, message = "{nickname.size}")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "{nickname.format}")
    private String username;

}
