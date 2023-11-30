package com.seungju.quiz.app.auth.dto;

import com.seungju.quiz.app.user.dto.UserDto;
import com.seungju.quiz.app.jwt.dto.TokenDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

public class AuthDto {

    @NoArgsConstructor
    @Getter
    @Setter
    @SuperBuilder
    public static class Login implements Serializable {
        private String username;
        private String password;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    @SuperBuilder
    public static class Join implements Serializable {
        private String email;
        private String name;
        private String username;
        private String password;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    @SuperBuilder
    public static class Logout implements Serializable {
        private String grantType;
        private String jti;
        private String refreshToken;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    @SuperBuilder
    public static class OauthToken implements Serializable {
        private String grantType;
        private String jti;
        private String refreshToken;
    }

    @NoArgsConstructor
    @Getter
    @Setter
    @SuperBuilder
    public static class LoginResponse extends TokenDto.Response implements Serializable {
        private UserDto.Response user;

        public LoginResponse(TokenDto.Response response) {
            this.setJti(response.getJti());
            this.setTokenType(response.getTokenType());
            this.setAccessToken(response.getAccessToken());
            this.setAccessTokenExpiresAt(response.getAccessTokenExpiresAt());
            this.setAccessTokenExpiresIn(response.getAccessTokenExpiresIn());
            this.setRefreshToken(response.getRefreshToken());
            this.setRefreshTokenExpiresAt(response.getRefreshTokenExpiresAt());
            this.setRefreshTokenExpiresIn(response.getRefreshTokenExpiresIn());
            this.setCreatedAt(response.getCreatedAt());
        }
    }

}
