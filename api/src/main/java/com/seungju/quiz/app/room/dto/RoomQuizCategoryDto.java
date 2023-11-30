package com.seungju.quiz.app.room.dto;

import com.seungju.quiz.app.quiz.dto.QuizCategoryDto;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class RoomQuizCategoryDto {
    @Getter
    @Setter
    public static class Response implements Serializable {
        private QuizCategoryDto.Response quizCategory;
    }

    @Getter
    @Setter
    public static class DetailResponse implements Serializable {
        private QuizCategoryDto.Response quizCategory;
    }
}
