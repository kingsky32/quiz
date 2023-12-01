package com.seungju.quiz.app.room.service;

import com.seungju.quiz.app.quiz.domain.*;
import com.seungju.quiz.app.room.domain.*;
import com.seungju.quiz.app.room.dto.RoomDto;
import com.seungju.quiz.app.room.dto.RoomDtoMapper;
import com.seungju.quiz.app.user.domain.Account;
import com.seungju.quiz.app.user.domain.AccountRepository;
import com.seungju.quiz.app.user.dto.UserDto;
import com.seungju.quiz.app.user.dto.UserDtoMapper;
import com.seungju.quiz.exception.ForbiddenException;
import com.seungju.quiz.exception.NotFoundException;
import com.seungju.quiz.pagniation.dto.PageableDto;
import com.seungju.quiz.restriction.repository.Restriction;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final QuizCategoryRepository quizCategoryRepository;
    private final RoomRepository roomRepository;
    private final AccountRepository accountRepository;
    private final RoomChatRepository roomChatRepository;
    private final QuizRepository quizRepository;
    private final RoomQuizRepository roomQuizRepository;
    private final RoomQuizCategoryRepository roomQuizCategoryRepository;
    private final RoomChatAnswerRepository roomChatAnswerRepository;
    private final SimpMessageSendingOperations simpMessageSendingOperations;
    private final List<InternalScheduler> internalSchedulers = new ArrayList<>();

    @Transactional
    public RoomDto.DetailResponse create(@NonNull RoomDto.Create create) {
        Room room = RoomDtoMapper.INSTANCE.toEntity(create);
        room.setCurrentNumber(0L);
        room.setStatus(Room.Status.READY);
        room = roomRepository.save(room);
        if (create.getQuizCategoryIds() != null) {
            List<RoomQuizCategory> roomQuizCategories = new ArrayList<>();
            for (Long quizCategoryId : create.getQuizCategoryIds()) {
                QuizCategory quizCategory = quizCategoryRepository.findById(quizCategoryId).orElseThrow(() -> new NotFoundException("Not found QuizCategory"));
                RoomQuizCategory roomQuizCategory = new RoomQuizCategory();
                RoomQuizCategory.Id roomQuizCategoryId = new RoomQuizCategory.Id();
                roomQuizCategoryId.setRoomId(room.getId());
                roomQuizCategoryId.setQuizCategoryId(quizCategory.getId());
                roomQuizCategory.setId(roomQuizCategoryId);
                roomQuizCategory.setRoom(room);
                roomQuizCategory.setQuizCategory(quizCategory);
                roomQuizCategory = roomQuizCategoryRepository.save(roomQuizCategory);
                roomQuizCategories.add(roomQuizCategory);
            }
            room.setRoomQuizCategories(roomQuizCategories);
        }
        return RoomDtoMapper.INSTANCE.toDetailResponse(room);
    }

    @Transactional
    public RoomDto.DetailResponse update(@NonNull Long id, @NonNull RoomDto.Update update) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new NotFoundException("Not found Room"));
        RoomDtoMapper.INSTANCE.merge(update, room);
        room = roomRepository.save(room);
        if (update.getQuizCategoryIds() != null) {
            roomQuizCategoryRepository.deleteAllByRoomId(id);
            List<RoomQuizCategory> roomQuizCategories = new ArrayList<>();
            for (Long quizCategoryId : update.getQuizCategoryIds()) {
                QuizCategory quizCategory = quizCategoryRepository.findById(quizCategoryId).orElseThrow(() -> new NotFoundException("Not found QuizCategory"));
                RoomQuizCategory roomQuizCategory = new RoomQuizCategory();
                RoomQuizCategory.Id roomQuizCategoryId = new RoomQuizCategory.Id();
                roomQuizCategoryId.setRoomId(room.getId());
                roomQuizCategoryId.setQuizCategoryId(quizCategory.getId());
                roomQuizCategory.setId(roomQuizCategoryId);
                roomQuizCategory.setRoom(room);
                roomQuizCategory.setQuizCategory(quizCategory);
                roomQuizCategory = roomQuizCategoryRepository.save(roomQuizCategory);
                roomQuizCategories.add(roomQuizCategory);
            }
            room.setRoomQuizCategories(roomQuizCategories);
        }
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
    public RoomDto.QuestionResponse getQuestion(@NonNull Room room) {
        Restriction restriction = new Restriction();
        restriction.in("quizCategory.id", room.getRoomQuizCategories().stream().map(x -> x.getQuizCategory().getId()).toList());
//        restriction.notIn("roomQuizzes.quiz.id", room.getRoomQuizzes().stream().map(x -> x.getQuiz().getId()).toList());
        Quiz quiz = quizRepository.findOne(restriction.output()).orElseThrow(() -> new NotFoundException("Not found Quiz"));
        RoomQuiz roomQuiz = new RoomQuiz();
        roomQuiz.setRoom(room);
        roomQuiz.setQuiz(quiz);
        roomQuiz = roomQuizRepository.save(roomQuiz);

        InternalScheduler internalScheduler = internalSchedulers.stream().filter(x -> room.getId().equals(x.getRoomId())).findFirst().orElseGet(() -> {
            InternalScheduler scheduler = new InternalScheduler();
            scheduler.setRoomId(room.getId());
            internalSchedulers.add(scheduler);
            return scheduler;
        });

        for (ScheduledFuture<?> scheduler : internalScheduler.schedulers) {
            scheduler.cancel(true);
        }
        internalScheduler.setSchedulers(new ArrayList<>());

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        // 소켓 힌트 넘김
        for (QuizHint quizHint : quiz.getQuizHints()) {
            RoomDto.HintResponse hintResponse = RoomDtoMapper.INSTANCE.toHintResponse(quizHint);
            ScheduledFuture<?> schedule = scheduler.schedule(
                    () -> {
                        simpMessageSendingOperations.convertAndSend("/ws/room/subscribe/" + room.getId() + "/hint", hintResponse);
                    },
                    quiz.getTimeoutMs() - quizHint.getExposedRemainTime() * 1000,
                    TimeUnit.MILLISECONDS
            );
            internalScheduler.schedulers.add(schedule);
        }
        // 소켓 정답 넘김
        {
            RoomDto.AnswerResponse answerResponse = new RoomDto.AnswerResponse();
            answerResponse.setAnswers(quiz.getQuizAnswers().stream().map(QuizAnswer::getAnswer).toList());
            ScheduledFuture<?> schedule = scheduler.schedule(
                    () -> {
                        simpMessageSendingOperations.convertAndSend("/ws/room/subscribe/" + room.getId() + "/answer", answerResponse);
                    },
                    quiz.getTimeoutMs(),
                    TimeUnit.MILLISECONDS
            );
            internalScheduler.schedulers.add(schedule);
        }
        // 문제 이후 5초 뒤 자동 스킵
        {
            ScheduledFuture<?> schedule = scheduler.schedule(
                    () -> {
                        skip(room.getId());
                    },
                    quiz.getTimeoutMs() + 5000,
                    TimeUnit.MILLISECONDS
            );
            internalScheduler.schedulers.add(schedule);
        }
        scheduler.shutdown();

        return RoomDtoMapper.INSTANCE.toQuestionResponse(roomQuiz.getQuiz());
    }

    @Transactional
    public void delete(@NonNull Long id) {
        roomRepository.deleteById(id);
    }

    @Transactional
    public void deleteAll(@NonNull RoomDto.DeleteAll deleteAll) {
        roomRepository.deleteAllById(deleteAll.getIds());
    }

    @Transactional
    public void play(@NonNull Long id) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new NotFoundException("Not found Room"));
        if (!room.getStatus().equals(Room.Status.READY)) {
            throw new ForbiddenException("can only start when the room is ready");
        }
        room.setStatus(Room.Status.PLAYING);
        roomRepository.save(room);
        simpMessageSendingOperations.convertAndSend("/ws/room/subscribe/" + id + "/question", this.getQuestion(room));
    }

    @Transactional
    public void skip(@NonNull Long id) {
        Room room = roomRepository.findById(id).orElseThrow(() -> new NotFoundException("Not found Room"));
        if (!room.getStatus().equals(Room.Status.PLAYING)) {
            throw new ForbiddenException("can only start when the room is playing");
        }
        if (room.getNumberOfQuiz() > room.getCurrentNumber()) {
            room.setCurrentNumber(room.getCurrentNumber() + 1);
        } else {
            room.setCurrentNumber(0L);
            room.setStatus(Room.Status.READY);
            internalSchedulers.stream().filter(x -> id.equals(x.getRoomId())).findFirst().ifPresent(x -> {
                for (ScheduledFuture<?> scheduler : x.schedulers) {
                    scheduler.cancel(true);
                }
                x.setSchedulers(new ArrayList<>());
            });
        }
        room = roomRepository.save(room);
        if (room.getStatus().equals(Room.Status.PLAYING)) {
            simpMessageSendingOperations.convertAndSend("/ws/room/subscribe/" + id + "/question", this.getQuestion(room));
        }
    }

    @Transactional
    public void chat(@NonNull Long id, @NonNull RoomDto.Chat chat) {
        RoomDto.ChatResponse response = new RoomDto.ChatResponse();
        Account account = accountRepository.findById(chat.getAccountId()).orElseThrow(() -> new NotFoundException("Not found Account"));
        UserDto.Response user = UserDtoMapper.INSTANCE.toResponse(account);
        user.setName(account.getUser().getName());
        response.setUser(user);
        response.setMessage(chat.getMessage());
        response.setIsCorrect(false);

        RoomChat roomChat = new RoomChat();
        Room room = roomRepository.findById(chat.getRoomId()).orElseThrow(() -> new NotFoundException("Not found Room"));
        roomChat.setRoom(room);
        roomChat.setContent(chat.getMessage());
        roomChat.setCreatedBy(account.getUser());
        roomChatRepository.save(roomChat);

        if (room.getStatus().equals(Room.Status.PLAYING)) {
            RoomQuiz roomQuiz = roomQuizRepository.findTopByRoomIdOrderByIdDesc(room.getId()).orElseThrow(() -> new NotFoundException("Not found RoomQuiz"));
            if (roomQuiz.getCreatedAt().plus(roomQuiz.getQuiz().getTimeoutMs(), ChronoUnit.MILLIS).isAfter(LocalDateTime.now())) {
                Optional<Boolean> optionalRoomChatAnswer = roomQuiz.getRoomChatAnswers().stream().map(x -> x.getRoomChat().getRoom().getId().equals(room.getId())).findFirst();
                if (optionalRoomChatAnswer.isEmpty()) {
                    for (QuizAnswer quizAnswer : roomQuiz.getQuiz().getQuizAnswers()) {
                        if (quizAnswer.getAnswer().equals(roomChat.getContent())) {
                            // 정답 저장
                            {
                                RoomChatAnswer roomChatAnswer = new RoomChatAnswer();
                                RoomChatAnswer.Id roomChatAnswerId = new RoomChatAnswer.Id();
                                roomChatAnswerId.setRoomQuizId(roomQuiz.getId());
                                roomChatAnswerId.setRoomChatId(roomChat.getId());
                                roomChatAnswer.setId(roomChatAnswerId);
                                roomChatAnswer.setRoomChat(roomChat);
                                roomChatAnswer.setRoomQuiz(roomQuiz);
                                roomChatAnswerRepository.save(roomChatAnswer);
                            }
                            response.setIsCorrect(true);
                            // 소켓 정답 넘김
                            {
                                RoomDto.AnswerResponse answerResponse = new RoomDto.AnswerResponse();
                                answerResponse.setAnswers(roomQuiz.getQuiz().getQuizAnswers().stream().map(QuizAnswer::getAnswer).toList());
                                simpMessageSendingOperations.convertAndSend("/ws/room/subscribe/" + room.getId() + "/answer", answerResponse);
                            }
                            break;
                        }
                    }
                }
            }
        }

        simpMessageSendingOperations.convertAndSend("/ws/room/subscribe/" + id + "/chat", response);
    }

    @Getter
    @Setter
    private static class InternalScheduler {
        private Long roomId;
        private List<ScheduledFuture<?>> schedulers = new ArrayList<>();
    }

}
