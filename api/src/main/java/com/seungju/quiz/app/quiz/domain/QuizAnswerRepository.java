package com.seungju.quiz.app.quiz.domain;

import com.seungju.quiz.jpa.domain.BaseRepository;

public interface QuizAnswerRepository extends BaseRepository<QuizAnswer, Long> {
    void deleteAllByQuizId(Long quizId);
}
