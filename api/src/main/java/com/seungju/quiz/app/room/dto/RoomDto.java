package com.seungju.quiz.app.room.dto;

import com.seungju.quiz.app.file.dto.FileDto;
import com.seungju.quiz.app.room.domain.Room;
import com.seungju.quiz.app.user.dto.UserDto;
import com.seungju.quiz.pagniation.dto.PageableDto;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class RoomDto {
    @Getter
    @Setter
    public static class Create implements Serializable {
        private String title;
        private Long numberOfQuiz;
        private Boolean isSecret;
        private String secretPassword;
        private List<Long> quizCategoryIds;
    }

    @Getter
    @Setter
    public static class Request extends PageableDto.Cursor.Request<Long> implements Serializable {
    }

    @Getter
    @Setter
    public static class Response implements Serializable {
        private Long id;
        private Room.Status status;
        private String title;
        private Long currentNumber;
        private Long numberOfQuiz;
        private Boolean isSecret;
        private List<RoomQuizCategoryDto.Response> roomQuizCategories;
        private UserDto.Response createdBy;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    public static class DetailResponse implements Serializable {
        private Long id;
        private Room.Status status;
        private String title;
        private Long currentNumber;
        private Long numberOfQuiz;
        private Boolean isSecret;
        private List<RoomQuizCategoryDto.DetailResponse> roomQuizCategories;
        private UserDto.Response createdBy;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    public static class Update implements Serializable {
        private String title;
        private Boolean isSecret;
        private Long numberOfQuiz;
        private String secretPassword;
        private List<Long> quizCategoryIds;
    }

    @Getter
    @Setter
    public static class DeleteAll implements Serializable {
        private List<Long> ids;
    }

    @Getter
    @Setter
    public static class Chat implements Serializable {
        private Long accountId;
        private Long roomId;
        private String message;
    }

    @Getter
    @Setter
    public static class ChatResponse implements Serializable {
        private UserDto.Response user;
        private Boolean isCorrect;
        private String message;
    }

    @Getter
    @Setter
    public static class QuestionResponse implements Serializable {
        private String title;
        private String content;
        private FileDto.Response soundFile;
        private Long timeoutMs;
        private UserDto.Response createdBy;
    }

    @Getter
    @Setter
    public static class HintResponse implements Serializable {
        private Long id;
        private String name;
        private String content;
    }

    @Getter
    @Setter
    public static class AnswerResponse implements Serializable {
        private List<String> answers;
    }

}
