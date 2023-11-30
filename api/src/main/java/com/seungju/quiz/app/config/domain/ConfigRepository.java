package com.seungju.quiz.app.config.domain;

import com.seungju.quiz.jpa.domain.BaseRepository;

import java.util.Optional;

public interface ConfigRepository extends BaseRepository<Config, Long> {

    Optional<Config> findTopByOrderByIdDesc();

}
