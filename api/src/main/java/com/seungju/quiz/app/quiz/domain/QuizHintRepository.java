package com.seungju.quiz.app.quiz.domain;

import com.seungju.quiz.jpa.domain.BaseRepository;

public interface QuizHintRepository extends BaseRepository<QuizHint, Long> {
    void deleteAllByQuizId(Long quizId);
}
