package com.seungju.quiz.app.room.domain;

import com.seungju.quiz.jpa.domain.BaseRepository;

import java.util.Optional;

public interface RoomQuizRepository extends BaseRepository<RoomQuiz, Long> {
    Optional<RoomQuiz> findTopByRoomIdOrderByIdDesc(Long id);
}
