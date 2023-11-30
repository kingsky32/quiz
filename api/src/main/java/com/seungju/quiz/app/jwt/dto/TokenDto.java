package com.seungju.quiz.app.jwt.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

public class TokenDto {

    @NoArgsConstructor
    @Getter
    @Setter
    @SuperBuilder
    public static class Response implements Serializable {
        private String jti;
        private String tokenType;
        private String accessToken;
        private LocalDateTime accessTokenExpiresAt;
        private Long accessTokenExpiresIn;
        private String refreshToken;
        private LocalDateTime refreshTokenExpiresAt;
        private Long refreshTokenExpiresIn;
        private LocalDateTime createdAt;
    }

}
