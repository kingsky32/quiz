package com.seungju.quiz.app.auth.dto;

import com.seungju.quiz.app.user.domain.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface AuthDtoMapper {
    AuthDtoMapper INSTANCE = Mappers.getMapper(AuthDtoMapper.class);

    @Mapping(source = "password", target = "password", ignore = true)
    Account toEntity(AuthDto.Join join);

}
