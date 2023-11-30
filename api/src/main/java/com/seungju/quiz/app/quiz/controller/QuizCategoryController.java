package com.seungju.quiz.app.quiz.controller;

import com.seungju.quiz.app.quiz.dto.QuizCategoryDto;
import com.seungju.quiz.app.quiz.service.QuizCategoryService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/quiz-category")
@RestController
public class QuizCategoryController {

    private final QuizCategoryService quizCategoryService;

    @PostMapping
    @Secured("ROLE_USER")
    public ResponseEntity<QuizCategoryDto.DetailResponse> create(@NonNull @RequestBody QuizCategoryDto.Create create) {
        return ResponseEntity.ok(quizCategoryService.create(create));
    }

    @GetMapping
    @Secured("ROLE_USER")
    public ResponseEntity<List<QuizCategoryDto.Response>> getAll() {
        return ResponseEntity.ok(quizCategoryService.getAll());
    }

    @GetMapping("/{id}")
    @Secured("ROLE_USER")
    public ResponseEntity<QuizCategoryDto.DetailResponse> get(@NonNull @PathVariable Long id) {
        return ResponseEntity.ok(quizCategoryService.get(id));
    }

    @PutMapping("/{id}")
    @Secured("ROLE_USER")
    public ResponseEntity<QuizCategoryDto.DetailResponse> update(@NonNull @PathVariable Long id, @NonNull @RequestBody QuizCategoryDto.Update update) {
        return ResponseEntity.ok(quizCategoryService.update(id, update));
    }

    @DeleteMapping("/{id}")
    @Secured("ROLE_USER")
    public void delete(@NonNull @PathVariable Long id) {
        quizCategoryService.delete(id);
    }

    @DeleteMapping
    @Secured("ROLE_USER")
    public void deleteAll(@NonNull QuizCategoryDto.DeleteAll deleteAll) {
        quizCategoryService.deleteAll(deleteAll);
    }

}
