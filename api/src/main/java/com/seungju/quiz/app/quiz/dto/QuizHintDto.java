package com.seungju.quiz.app.quiz.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

public class QuizHintDto {
    @Getter
    @Setter
    public static class Response implements Serializable {
        private Long id;
        private String name;
        private String content;
        private Long exposedRemainTime;
    }
}
