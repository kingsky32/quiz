package com.seungju.quiz.app.jwt.service;

import com.seungju.quiz.app.auth.domain.CustomUserDetails;
import com.seungju.quiz.app.jwt.constant.JwtConstant;
import com.seungju.quiz.app.jwt.domain.RefreshToken;
import com.seungju.quiz.app.jwt.domain.RefreshTokenRedisRepository;
import com.seungju.quiz.app.jwt.dto.TokenDto;
import com.seungju.quiz.app.jwt.exception.InvalidAccessTokenException;
import com.seungju.quiz.exception.BadRequestException;
import com.seungju.quiz.exception.NotFoundException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class JwtService {
    private final SecretKey secretKey;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;
    @Value("${jwt.access-token-expires}")
    private int ACCESS_TOKEN_EXPIRES;
    @Value("${jwt.refresh-token-expires}")
    private int REFRESH_TOKEN_EXPIRES;

    public RefreshToken generateRefreshToken(final Long userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + REFRESH_TOKEN_EXPIRES);

        RefreshToken refreshToken = RefreshToken.builder()
                .jti(UUID.randomUUID().toString())
                .refreshToken(Jwts.builder().signWith(secretKey).setIssuedAt(now)
                        .setExpiration(expiration).setSubject(userId.toString()).compact())
                .userId(userId)
                .build();

        refreshTokenRedisRepository.save(refreshToken);

        return refreshToken;
    }

    public String generateAccessToken(final String jti) {
        RefreshToken refreshToken = refreshTokenRedisRepository.findById(jti)
                .orElseThrow(() -> new NotFoundException("RefreshToken 이 존재하지 않습니다"));

        Long userId = refreshToken.getUserId();

        Date now = new Date();
        Date expiration = new Date(now.getTime() + ACCESS_TOKEN_EXPIRES);

        return Jwts.builder().signWith(secretKey).setIssuedAt(now)
                .setExpiration(expiration).setSubject(userId.toString()).compact();
    }

    public TokenDto.Response generateToken(final Long userId) {
        RefreshToken refreshToken = generateRefreshToken(userId);
        String accessToken = generateAccessToken(refreshToken.getJti());

        Date now = new Date();

        Date expiration = extract(accessToken).getExpiration();
        Long refreshTokenExpiresIn = refreshTokenRedisRepository.getExpiresIn(refreshToken.getJti());

        return TokenDto.Response.builder()
                .tokenType(JwtConstant.TOKEN_TYPE)
                .jti(refreshToken.getJti())
                .accessToken(accessToken)
                .accessTokenExpiresIn(expiration.getTime() - now.getTime())
                .accessTokenExpiresAt(Instant.ofEpochMilli(expiration.getTime())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime())
                .refreshToken(refreshToken.getRefreshToken())
                .refreshTokenExpiresIn(refreshTokenExpiresIn)
                .refreshTokenExpiresAt(Instant.ofEpochMilli(now.getTime() + refreshTokenExpiresIn)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime())
                .createdAt(LocalDateTime.ofInstant(now.toInstant(), ZoneId.systemDefault()))
                .build();
    }

    public TokenDto.Response renewalToken(final String jti, final String refreshToken) {
        RefreshToken refreshTokenEntity = refreshTokenRedisRepository.findById(jti).orElseThrow(() -> new NotFoundException("RefreshToken 이 존재하지 않습니다"));
        if (!refreshTokenEntity.getRefreshToken().equals(refreshToken)) {
            throw new BadRequestException("올바르지 않은 RefreshToken 입니다");
        }
        return generateToken(refreshTokenEntity.getUserId());
    }

    public Boolean expireToken(final String jti, final String refreshToken, final CustomUserDetails user) {
        RefreshToken refreshTokenEntity = refreshTokenRedisRepository.findById(jti).orElseThrow(() -> new NotFoundException("RefreshToken 이 존재하지 않습니다"));
        if (!refreshTokenEntity.getRefreshToken().equals(refreshToken)) {
            throw new BadRequestException("올바르지 않은 RefreshToken 입니다");
        }
        if (!refreshTokenEntity.getUserId().equals(user.getAccount().getId())) {
            throw new BadRequestException("올바르지 않은 User 입니다");
        }
        return refreshTokenRedisRepository.deleteById(jti);
    }

    public Claims extract(final String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(secretKey).build()
                    .parseClaimsJws(token).getBody();
        } catch (final JwtException e) {
            throw new InvalidAccessTokenException();
        }
    }

    public Long extractId(final String token) {
        return Long.parseLong(extract(token).getSubject());
    }
}
