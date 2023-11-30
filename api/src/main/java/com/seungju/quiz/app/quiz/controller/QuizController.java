package com.seungju.quiz.app.quiz.controller;

import com.seungju.quiz.app.quiz.dto.QuizDto;
import com.seungju.quiz.app.quiz.service.QuizService;
import com.seungju.quiz.pagniation.dto.PageableDto;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/quiz")
@RestController
public class QuizController {

    private final QuizService quizService;

    @PostMapping
    @Secured("ROLE_USER")
    public ResponseEntity<QuizDto.DetailResponse> create(@NonNull @RequestBody QuizDto.Create create) {
        return ResponseEntity.ok(quizService.create(create));
    }

    @GetMapping
    @Secured("ROLE_USER")
    public ResponseEntity<PageableDto.Page.Response<QuizDto.Response>> getAll(@NonNull QuizDto.Request request) {
        return ResponseEntity.ok(quizService.getAll(request));
    }

    @GetMapping("/{id}")
    @Secured("ROLE_USER")
    public ResponseEntity<QuizDto.DetailResponse> get(@NonNull @PathVariable Long id) {
        return ResponseEntity.ok(quizService.get(id));
    }

    @PutMapping("/{id}")
    @Secured("ROLE_USER")
    public ResponseEntity<QuizDto.DetailResponse> update(@NonNull @PathVariable Long id, @NonNull @RequestBody QuizDto.Update update) {
        return ResponseEntity.ok(quizService.update(id, update));
    }

    @DeleteMapping("/{id}")
    @Secured("ROLE_USER")
    public void delete(@NonNull @PathVariable Long id) {
        quizService.delete(id);
    }

    @DeleteMapping
    @Secured("ROLE_USER")
    public void deleteAll(@NonNull QuizDto.DeleteAll deleteAll) {
        quizService.deleteAll(deleteAll);
    }

}
