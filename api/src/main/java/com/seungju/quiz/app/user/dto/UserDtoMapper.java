package com.seungju.quiz.app.user.dto;

import com.seungju.quiz.app.user.domain.Account;
import com.seungju.quiz.app.user.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserDtoMapper {
    UserDtoMapper INSTANCE = Mappers.getMapper(UserDtoMapper.class);

    UserDto.Response toResponse(Account account);

    UserDto.Response toResponse(User user);

}
