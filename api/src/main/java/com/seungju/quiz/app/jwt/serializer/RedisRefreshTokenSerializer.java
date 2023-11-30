package com.seungju.quiz.app.jwt.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seungju.quiz.app.jwt.domain.RefreshToken;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class RedisRefreshTokenSerializer implements RedisSerializer<RefreshToken> {
    private final ObjectMapper objectMapper;

    public RedisRefreshTokenSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public byte[] serialize(RefreshToken refreshToken) throws SerializationException {
        try {
            return objectMapper.writeValueAsBytes(refreshToken);
        } catch (Exception e) {
            throw new SerializationException("Error serializing RefreshToken to JSON.", e);
        }
    }

    @Override
    public RefreshToken deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        try {
            return objectMapper.readValue(bytes, RefreshToken.class);
        } catch (Exception e) {
            throw new SerializationException("Error deserializing RefreshToken from JSON.", e);
        }
    }
}
