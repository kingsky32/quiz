package com.seungju.quiz.app.file.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "file")
@EntityListeners(AuditingEntityListener.class)
public class File {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "status", columnDefinition = "ENUM ('READY', 'PROGRESS', 'DONE', 'ERROR)", nullable = false)
  private Status status = Status.READY;

  @Size(max = 255)
  @Column(name = "upload_id")
  private String uploadId;

  @Size(max = 255)
  @NotNull
  @Column(name = "name", nullable = false)
  private String name;

  @Size(max = 20)
  @NotNull
  @Column(name = "extension", nullable = false, length = 20)
  private String extension;

  @Size(max = 255)
  @NotNull
  @Column(name = "server_path", nullable = false)
  private String serverPath;

  @Size(max = 255)
  @NotNull
  @Column(name = "content_type", nullable = false)
  private String contentType;

  @NotNull
  @Column(name = "size", columnDefinition = "BIGINT", nullable = false)
  private Long size;

  @NotNull
  @Column(name = "created_at", nullable = false)
  @CreatedDate
  private LocalDateTime createdAt;

  @Transient
  private static String extractExtension(String filename) {
    if (filename.contains(".")) {
      return filename.substring(filename.lastIndexOf(".") + 1);
    }
    return StringUtils.EMPTY;
  }

  @Transient
  public String getServerPath() {
    return this.serverPath + "/" + this.getId() + "/" + this.getServerFilename();
  }

  @Transient
  public String getServerFilename() {
    String serverFileName = "source";
    if (StringUtils.isNotEmpty(this.extension)) {
      serverFileName += "." + this.extension;
    }
    return serverFileName;
  }

  public enum Status {
    READY, PROGRESS, DONE, ERROR,
  }
}
