package com.seungju.quiz.app.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class UserDto {
  @NoArgsConstructor
  @Getter
  @Setter
  public static class Response {
    private Long id;
    private String username;
    private String name;
  }

}
