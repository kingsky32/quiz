package com.seungju.quiz.app.room.dto;

import com.seungju.quiz.app.room.domain.RoomQuizCategory;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface RoomQuizCategoryDtoMapper {

    RoomQuizCategoryDtoMapper INSTANCE = Mappers.getMapper(RoomQuizCategoryDtoMapper.class);

    RoomQuizCategoryDto.Response toResponse(RoomQuizCategory roomQuizCategory);

    RoomQuizCategoryDto.DetailResponse toDetailResponse(RoomQuizCategory roomQuizCategory);

}
