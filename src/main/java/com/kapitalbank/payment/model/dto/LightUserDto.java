package com.kapitalbank.payment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LightUserDto {

    private Long id;
    private String username;
    private String email;
    private String createdAt;
    private Boolean isActive;

}
