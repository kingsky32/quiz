package com.seungju.quiz.app.quiz.service;

import com.seungju.quiz.app.file.domain.File;
import com.seungju.quiz.app.file.domain.FileRepository;
import com.seungju.quiz.app.quiz.domain.*;
import com.seungju.quiz.app.quiz.dto.QuizDto;
import com.seungju.quiz.app.quiz.dto.QuizDtoMapper;
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
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizCategoryRepository quizCategoryRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final QuizHintRepository quizHintRepository;
    private final FileRepository fileRepository;

    @Transactional
    public QuizDto.DetailResponse create(@NonNull QuizDto.Create create) {
        Quiz quiz = QuizDtoMapper.INSTANCE.toEntity(create);
        if (create.getQuizCategoryId() != null) {
            QuizCategory quizCategory = quizCategoryRepository.findById(create.getQuizCategoryId()).orElseThrow(() -> new NotFoundException("Not found QuizCategory"));
            quiz.setQuizCategory(quizCategory);
        }
        if (create.getSoundFileId() != null) {
            File soundFile = fileRepository.findById(create.getSoundFileId()).orElseThrow(() -> new NotFoundException("Not found File"));
            quiz.setSoundFile(soundFile);
        }
        quiz = quizRepository.save(quiz);
        if (create.getHints() != null) {
            List<QuizHint> quizHints = new ArrayList<>();
            for (QuizDto.Create.Hint hint : create.getHints()) {
                QuizHint quizHint = QuizDtoMapper.INSTANCE.toEntity(hint);
                quizHint.setQuiz(quiz);
                quizHint = quizHintRepository.save(quizHint);
                quizHints.add(quizHint);
            }
            quiz.setQuizHints(quizHints);
        }
        if (create.getAnswers() != null) {
            List<QuizAnswer> quizAnswers = new ArrayList<>();
            for (QuizDto.Create.Answer answer : create.getAnswers()) {
                QuizAnswer quizAnswer = QuizDtoMapper.INSTANCE.toEntity(answer);
                quizAnswer.setQuiz(quiz);
                quizAnswer = quizAnswerRepository.save(quizAnswer);
                quizAnswers.add(quizAnswer);
            }
            quiz.setQuizAnswers(quizAnswers);
        }
        return QuizDtoMapper.INSTANCE.toDetailResponse(quiz);
    }

    @Transactional(readOnly = true)
    public PageableDto.Page.Response<QuizDto.Response> getAll(@NonNull QuizDto.Request request) {
        Restriction restriction = new Restriction();
        Page<Quiz> quizPage = quizRepository.findAll(restriction.output(), PageableDto.Page.Request.of(request));
        return PageableDto.Page.Response.of(quizPage, quizPage.stream().map(QuizDtoMapper.INSTANCE::toResponse).toList());
    }

    @Transactional(readOnly = true)
    public QuizDto.DetailResponse get(@NonNull Long id) {
        Quiz quiz = quizRepository.findById(id).orElseThrow(() -> new NotFoundException("Not found Quiz"));
        return QuizDtoMapper.INSTANCE.toDetailResponse(quiz);
    }

    @Transactional
    public QuizDto.DetailResponse update(@NonNull Long id, @NonNull QuizDto.Update update) {
        Quiz quiz = quizRepository.findById(id).orElseThrow(() -> new NotFoundException("Not found Quiz"));
        QuizDtoMapper.INSTANCE.merge(update, quiz);
        if (update.getQuizCategoryId() != null) {
            QuizCategory quizCategory = quizCategoryRepository.findById(update.getQuizCategoryId()).orElseThrow(() -> new NotFoundException("Not found QuizCategory"));
            quiz.setQuizCategory(quizCategory);
        }
        if (update.getSoundFileId() != null) {
            File soundFile = fileRepository.findById(update.getSoundFileId()).orElseThrow(() -> new NotFoundException("Not found File"));
            quiz.setSoundFile(soundFile);
        }
        quiz = quizRepository.save(quiz);
        if (update.getHints() != null) {
            quizHintRepository.deleteAllByQuizId(quiz.getId());
            List<QuizHint> quizHints = new ArrayList<>();
            for (QuizDto.Create.Hint hint : update.getHints()) {
                QuizHint quizHint = QuizDtoMapper.INSTANCE.toEntity(hint);
                quizHint.setQuiz(quiz);
                quizHint = quizHintRepository.save(quizHint);
                quizHints.add(quizHint);
            }
            quiz.setQuizHints(quizHints);
        }
        if (update.getAnswers() != null) {
            quizAnswerRepository.deleteAllByQuizId(quiz.getId());
            List<QuizAnswer> quizAnswers = new ArrayList<>();
            for (QuizDto.Create.Answer answer : update.getAnswers()) {
                QuizAnswer quizAnswer = QuizDtoMapper.INSTANCE.toEntity(answer);
                quizAnswer.setQuiz(quiz);
                quizAnswer = quizAnswerRepository.save(quizAnswer);
                quizAnswers.add(quizAnswer);
            }
            quiz.setQuizAnswers(quizAnswers);
        }
        return QuizDtoMapper.INSTANCE.toDetailResponse(quiz);
    }

    @Transactional
    public void delete(@NonNull Long id) {
        quizRepository.deleteById(id);
    }

    @Transactional
    public void deleteAll(@NonNull QuizDto.DeleteAll deleteAll) {
        quizRepository.deleteAllById(deleteAll.getIds());
    }

}
