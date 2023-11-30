package com.seungju.quiz.app.user.domain;

import com.seungju.quiz.jpa.domain.BaseRepository;

import java.util.Optional;

public interface AccountRepository extends BaseRepository<Account, Long> {

    Optional<Account> findByUsername(String username);
}
