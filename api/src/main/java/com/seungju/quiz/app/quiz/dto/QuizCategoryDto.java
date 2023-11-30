package com.seungju.quiz.app.quiz.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

public class QuizCategoryDto {

    @Getter
    @Setter
    public static class Create implements Serializable {
        private Long parentId;
        private String name;
    }

    @Getter
    @Setter
    public static class Response implements Serializable {
        private Long id;
        private String name;
        private List<Response> children;
    }

    @Getter
    @Setter
    public static class DetailResponse implements Serializable {
        private Long id;
        private String name;
        private List<DetailResponse> children;
    }

    @Getter
    @Setter
    public static class Update implements Serializable {
        private Long parentId;
        private String name;
    }

    @Getter
    @Setter
    public static class DeleteAll implements Serializable {
        private List<Long> ids;
    }

}
