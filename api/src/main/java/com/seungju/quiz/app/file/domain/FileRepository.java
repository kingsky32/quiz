package com.seungju.quiz.app.file.domain;

import com.seungju.quiz.jpa.domain.BaseRepository;

import java.util.Optional;

public interface FileRepository extends BaseRepository<File, Long> {
    Optional<File> findByIdAndStatus(Long id, File.Status status);
}
