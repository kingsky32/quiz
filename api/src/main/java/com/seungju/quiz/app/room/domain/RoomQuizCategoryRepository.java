package com.seungju.quiz.app.room.domain;

import com.seungju.quiz.jpa.domain.BaseRepository;

public interface RoomQuizCategoryRepository extends BaseRepository<RoomQuizCategory, RoomQuizCategory.Id> {
    void deleteAllByRoomId(Long id);
}
