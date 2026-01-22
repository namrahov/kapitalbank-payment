package com.kapitalbank.payment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {

    private Long id;
    private String email;
    private String nickname;
    private String description;

}
