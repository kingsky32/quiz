package com.seungju.quiz.app.quiz.dto;

import com.seungju.quiz.app.quiz.domain.Quiz;
import com.seungju.quiz.app.quiz.domain.QuizAnswer;
import com.seungju.quiz.app.quiz.domain.QuizHint;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface QuizDtoMapper {

    QuizDtoMapper INSTANCE = Mappers.getMapper(QuizDtoMapper.class);

    Quiz toEntity(QuizDto.Create create);

    QuizHint toEntity(QuizDto.Create.Hint hint);

    QuizAnswer toEntity(QuizDto.Create.Answer answer);

    QuizHint toEntity(QuizDto.Update.Hint hint);

    QuizAnswer toEntity(QuizDto.Update.Answer answer);

    void merge(QuizDto.Update update, @MappingTarget Quiz quiz);

    QuizDto.Response toResponse(Quiz quiz);

    QuizDto.DetailResponse toDetailResponse(Quiz quiz);

}
