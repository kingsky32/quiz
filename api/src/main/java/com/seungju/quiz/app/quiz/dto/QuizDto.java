package com.seungju.quiz.app.quiz.dto;

import com.seungju.quiz.app.file.dto.FileDto;
import com.seungju.quiz.app.user.dto.UserDto;
import com.seungju.quiz.pagniation.dto.PageableDto;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class QuizDto {
    @Getter
    @Setter
    public static class Create implements Serializable {
        private Long quizCategoryId;
        private String title;
        private String content;
        private Long soundFileId;
        private Long timeoutMs;
        private Boolean isActive;
        private List<Hint> hints;
        private List<Answer> answers;

        @Getter
        @Setter
        public static class Hint implements Serializable {
            private String name;
            private String content;
        }

        @Getter
        @Setter
        public static class Answer implements Serializable {
            private String answer;
        }
    }

    @Getter
    @Setter
    public static class Request extends PageableDto.Page.Request implements Serializable {
    }

    @Getter
    @Setter
    public static class Response implements Serializable {
        private Long id;
        private QuizCategoryDto.Response quizCategory;
        private String title;
        private String content;
        private FileDto.Response soundFile;
        private Long timeoutMs;
        private Boolean isActive;
        private UserDto.Response createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter
    @Setter
    public static class DetailResponse implements Serializable {
        private Long id;
        private QuizCategoryDto.DetailResponse quizCategory;
        private String title;
        private String content;
        private FileDto.Response soundFile;
        private Long timeoutMs;
        private Boolean isActive;
        private List<QuizHintDto.Response> quizHints;
        private List<QuizAnswerDto.Response> quizAnswers;
        private UserDto.Response createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Getter
    @Setter
    public static class Update implements Serializable {
        private Long quizCategoryId;
        private String title;
        private String content;
        private Long soundFileId;
        private Long timeoutMs;
        private Boolean isActive;
        private List<Create.Hint> hints;
        private List<Create.Answer> answers;

        @Getter
        @Setter
        public static class Hint implements Serializable {
            private String name;
            private String content;
        }

        @Getter
        @Setter
        public static class Answer implements Serializable {
            private String answer;
        }
    }

    @Getter
    @Setter
    public static class DeleteAll implements Serializable {
        private List<Long> ids;
    }
}
