package com.seungju.quiz.app.auth.controller;

import com.seungju.quiz.app.user.dto.UserDto;
import com.seungju.quiz.app.auth.domain.CustomUserDetails;
import com.seungju.quiz.app.auth.dto.AuthDto;
import com.seungju.quiz.app.auth.service.AuthService;
import com.seungju.quiz.app.jwt.dto.TokenDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/auth")
@RestController
public class AuthController {

  private final AuthService authService;

  @GetMapping("/me")
  @Secured("ROLE_USER")
  public ResponseEntity<UserDto.Response> me(
          @AuthenticationPrincipal final CustomUserDetails user
  ) {
    return ResponseEntity.ok(authService.me(user.getAccount()));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthDto.LoginResponse> login(
          @RequestBody final AuthDto.Login login
  ) {
    return ResponseEntity.ok(authService.login(login));
  }

  @PostMapping("/join")
  public ResponseEntity<AuthDto.LoginResponse> join(
          @RequestBody final AuthDto.Join join
  ) {
    return ResponseEntity.ok(authService.join(join));
  }

  @PostMapping("/logout")
  @Secured("ROLE_USER")
  public ResponseEntity<Boolean> logout(
          @RequestBody final AuthDto.Logout request,
          @AuthenticationPrincipal final CustomUserDetails user
  ) {
    return ResponseEntity.ok(authService.logout(request, user));
  }

  @PostMapping("/oauth/token")
  public ResponseEntity<TokenDto.Response> oauthToken(
          @RequestBody final AuthDto.OauthToken request
  ) {
    return ResponseEntity.ok(authService.oauthToken(request));
  }

}
