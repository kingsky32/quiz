package com.seungju.quiz.app.jwt.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seungju.quiz.app.jwt.serializer.RedisRefreshTokenSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
public class RefreshTokenRedisRepository {
    private final RedisTemplate<String, RefreshToken> redisTemplate;
    @Value("${jwt.refresh-token-expires}")
    private int refreshTokenExpires;

    public RefreshTokenRedisRepository(RedisTemplate<String, RefreshToken> redisTemplate) {
        this.redisTemplate = redisTemplate;

        ObjectMapper objectMapper = new ObjectMapper();
        RedisRefreshTokenSerializer jsonSerializer = new RedisRefreshTokenSerializer(objectMapper);

        redisTemplate.setDefaultSerializer(jsonSerializer);
        redisTemplate.setValueSerializer(jsonSerializer);
        redisTemplate.setHashValueSerializer(jsonSerializer);
    }

    public void save(final RefreshToken refreshToken) {
        ValueOperations<String, RefreshToken> valueOperations = redisTemplate.opsForValue();
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(RefreshToken.class));
        valueOperations.set(refreshToken.getJti(), refreshToken);
        redisTemplate.expire(refreshToken.getJti(), refreshTokenExpires, TimeUnit.MILLISECONDS);
    }

    public Optional<RefreshToken> findById(final String jti) {
        ValueOperations<String, RefreshToken> valueOperations = redisTemplate.opsForValue();
        RefreshToken refreshToken = valueOperations.get(jti);

        if (refreshToken == null) {
            return Optional.empty();
        }

        return Optional.of(
                RefreshToken.builder()
                        .jti(jti)
                        .refreshToken(refreshToken.getRefreshToken())
                        .userId(refreshToken.getUserId())
                        .build()
        );
    }

    public Long getExpiresIn(final String id) {
        return redisTemplate.getExpire(id, TimeUnit.MILLISECONDS);
    }

    public Boolean deleteById(final String id) {
        return redisTemplate.delete(id);
    }

}
