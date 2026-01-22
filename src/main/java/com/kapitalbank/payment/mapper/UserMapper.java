package com.kapitalbank.payment.mapper;

import com.kapitalbank.payment.dao.entity.User;
import com.kapitalbank.payment.model.dto.LightUserDto;
import com.kapitalbank.payment.model.dto.UserResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(source = "user.username", target = "nickname")
    UserResponseDto toDto(User user);


    default String convertCreatedAt(LocalDateTime createdAt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return createdAt.format(formatter);
    }

}
