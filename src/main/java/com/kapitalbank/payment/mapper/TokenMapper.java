package com.kapitalbank.payment.mapper;


import com.kapitalbank.payment.dao.entity.Token;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface TokenMapper {

    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    Token toToken(String activationToken, Long userId);

}
