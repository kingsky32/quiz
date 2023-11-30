package com.seungju.quiz.app.jwt.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Builder
@RedisHash(value = "refresh-token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    @Id
    private String jti;

    private String refreshToken;

    private Long userId;

}
