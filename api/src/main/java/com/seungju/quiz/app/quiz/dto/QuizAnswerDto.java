package com.seungju.quiz.app.quiz.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class QuizAnswerDto {
    @Getter
    @Setter
    public static class Response implements Serializable {
        private Long id;
        private String answer;
    }
}
