package com.seungju.quiz.app.quiz.dto;

import com.seungju.quiz.app.quiz.domain.QuizCategory;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface QuizCategoryDtoMapper {

    QuizCategoryDtoMapper INSTANCE = Mappers.getMapper(QuizCategoryDtoMapper.class);

    QuizCategory toEntity(QuizCategoryDto.Create save);

    void merge(QuizCategoryDto.Update update, @MappingTarget QuizCategory quizCategory);

    QuizCategoryDto.Response toResponse(QuizCategory quizCategory);

    QuizCategoryDto.DetailResponse toDetailResponse(QuizCategory quizCategory);

}
