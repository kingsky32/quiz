package com.seungju.quiz.app.file.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class FileDto {

  @NoArgsConstructor
  @Getter
  @Setter
  public static class Response implements Serializable {
    private Long id;
    private String name;
    private String extension;
    private String serverPath;
    private String contentType;
    private Long size;
    private LocalDateTime createdAt;
  }

  @NoArgsConstructor
  @Getter
  @Setter
  public static class CreateMultipartUpload implements Serializable {
    private String name;
    private String contentType;
    private Long size;
  }

  @NoArgsConstructor
  @Getter
  @Setter
  public static class CreateMultipartUploadResponse implements Serializable {
    private Long id;
  }

  @NoArgsConstructor
  @Getter
  @Setter
  public static class UploadPartResponse implements Serializable {
    private String etag;
  }

  @NoArgsConstructor
  @Getter
  @Setter
  public static class CompleteMultipartUploadPart implements Serializable {
    private String etag;
    private Integer partNumber;
  }

  @NoArgsConstructor
  @Getter
  @Setter
  public static class CompleteMultipartUpload implements Serializable {
    private List<CompleteMultipartUploadPart> parts;
  }

}
