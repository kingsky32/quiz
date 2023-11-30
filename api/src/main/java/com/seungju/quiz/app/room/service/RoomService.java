package com.seungju.quiz.app.room.service;

import com.seungju.quiz.app.quiz.domain.QuizCategory;
import com.seungju.quiz.app.quiz.domain.QuizCategoryRepository;
import com.seungju.quiz.app.room.domain.Room;
import com.seungju.quiz.app.room.domain.RoomQuizCategory;
import com.seungju.quiz.app.room.domain.RoomRepository;
import com.seungju.quiz.app.room.dto.RoomDto;
import com.seungju.quiz.app.room.dto.RoomDtoMapper;
import com.seungju.quiz.exception.NotFoundException;
import com.seungju.quiz.pagniation.dto.PageableDto;
import com.seungju.quiz.restriction.repository.Restriction;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final QuizCategoryRepository quizCategoryRepository;
    private final RoomRepository roomRepository;

    @Transactional
    public RoomDto.DetailResponse create(@NonNull RoomDto.Create create) {
        Room room = RoomDtoMapper.INSTANCE.toEntity(create);
        if (create.getQuizCategoryIds() != null) {
            List<RoomQuizCategory> roomQuizCategories = new ArrayList<>();
            for (Long quizCategoryId : create.getQuizCategoryIds()) {
                QuizCategory quizCategory = quizCategoryRepository.findById(quizCategoryId).orElseThrow(() -> new NotFoundException("Not found QuizCategory"));
                RoomQuizCategory roomQuizCategory = new RoomQuizCategory();
                roomQuizCategory.setRoom(room);
                roomQuizCategory.setQuizCategory(quizCategory);
                roomQuizCategories.add(roomQuizCategory);
            }
            room.setRoomQuizCategories(roomQuizCategories);
        }
        room = roomRepository.save(room);
        return RoomDtoMapper.INSTANCE.toDetailResponse(room);
    }

    @Transactional(readOnly = true)
    public PageableDto.Cursor.Response<Long, RoomDto.Response> getAll(@NonNull RoomDto.Request request) {
        Page<Room> roomPage;
        Restriction restriction = new Restriction();
        if (request.getCursor() != null) {
            restriction.gne("id", request.getCursor());
        }
        roomPage = roomRepository.findAll(restriction.output(), request.getPageable());
        return PageableDto.Cursor.Response.of(
                request.getCursor(),
                roomPage.isLast() ? null : roomPage.getContent().get(roomPage.getContent().size() - 1).getId(),
                roomPage,
                roomPage.stream().map(RoomDtoMapper.INSTANCE::toResponse).toList()
        );
    }

    @Transactional(readOnly = true)
    public RoomDto.DetailResponse get(@NonNull Long id) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new NotFoundException("Not found Room"));
        return RoomDtoMapper.INSTANCE.toDetailResponse(room);
    }

    @Transactional
    public RoomDto.DetailResponse update(@NonNull Long id, @NonNull RoomDto.Update update) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new NotFoundException("Not found Room"));
        RoomDtoMapper.INSTANCE.merge(update, room);
        if (update.getQuizCategoryIds() != null) {
            List<RoomQuizCategory> roomQuizCategories = new ArrayList<>();
            for (Long quizCategoryId : update.getQuizCategoryIds()) {
                QuizCategory quizCategory = quizCategoryRepository.findById(quizCategoryId).orElseThrow(() -> new NotFoundException("Not found QuizCategory"));
                RoomQuizCategory roomQuizCategory = new RoomQuizCategory();
                roomQuizCategory.setRoom(room);
                roomQuizCategory.setQuizCategory(quizCategory);
                roomQuizCategories.add(roomQuizCategory);
            }
            room.setRoomQuizCategories(roomQuizCategories);
        }
        room = roomRepository.save(room);
        return RoomDtoMapper.INSTANCE.toDetailResponse(room);
    }

    @Transactional
    public void delete(@NonNull Long id) {
        roomRepository.deleteById(id);
    }

    @Transactional
    public void deleteAll(@NonNull RoomDto.DeleteAll deleteAll) {
        roomRepository.deleteAllById(deleteAll.getIds());
    }

}
