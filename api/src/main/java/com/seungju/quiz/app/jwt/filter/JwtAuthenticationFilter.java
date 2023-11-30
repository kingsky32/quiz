package com.seungju.quiz.app.jwt.filter;

import com.seungju.quiz.app.user.domain.Account;
import com.seungju.quiz.app.user.domain.AccountRepository;
import com.seungju.quiz.app.auth.domain.CustomUserDetails;
import com.seungju.quiz.app.jwt.constant.JwtConstant;
import com.seungju.quiz.app.jwt.exception.InvalidAccessTokenException;
import com.seungju.quiz.app.jwt.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final AccountRepository accountRepository;

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(JwtConstant.AUTH_HEADER);

        if (authHeader == null || !authHeader.toLowerCase()
                .startsWith(JwtConstant.TOKEN_TYPE.toLowerCase()) || authHeader.length() < 7) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = authHeader.substring(7);
        Long id = jwtService.extractId(accessToken);

        if (id != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Account account = accountRepository.findById(id).orElseThrow(InvalidAccessTokenException::new);
            CustomUserDetails userDetails = new CustomUserDetails(account);
            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails,
                    userDetails.getPassword(), userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
