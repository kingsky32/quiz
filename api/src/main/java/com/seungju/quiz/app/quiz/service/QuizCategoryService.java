package com.seungju.quiz.app.quiz.service;

import com.seungju.quiz.app.quiz.domain.QuizCategory;
import com.seungju.quiz.app.quiz.domain.QuizCategoryRepository;
import com.seungju.quiz.app.quiz.dto.QuizCategoryDto;
import com.seungju.quiz.app.quiz.dto.QuizCategoryDtoMapper;
import com.seungju.quiz.exception.NotFoundException;
import com.seungju.quiz.restriction.repository.Restriction;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizCategoryService {

    private final QuizCategoryRepository quizCategoryRepository;

    private QuizCategoryDto.Response toResponse(@NonNull QuizCategory quizCategory) {
        QuizCategoryDto.Response response = QuizCategoryDtoMapper.INSTANCE.toResponse(quizCategory);
        if (!quizCategory.getQuizCategories().isEmpty()) {
            response.setChildren(quizCategory.getQuizCategories().stream().map(this::toResponse).toList());
        }
        return response;
    }

    private QuizCategoryDto.DetailResponse toDetailResponse(@NonNull QuizCategory quizCategory) {
        QuizCategoryDto.DetailResponse detailResponse = QuizCategoryDtoMapper.INSTANCE.toDetailResponse(quizCategory);
        if (!quizCategory.getQuizCategories().isEmpty()) {
            detailResponse.setChildren(quizCategory.getQuizCategories().stream().map(this::toDetailResponse).toList());
        }
        return detailResponse;
    }

    @Transactional
    public QuizCategoryDto.DetailResponse create(@NonNull QuizCategoryDto.Create create) {
        QuizCategory quizCategory = QuizCategoryDtoMapper.INSTANCE.toEntity(create);
        if (create.getParentId() != null) {
            QuizCategory parent = quizCategoryRepository.findById(create.getParentId()).orElseThrow(() -> new NotFoundException("Not found QuizCategory"));
            quizCategory.setParent(parent);
        }
        quizCategory = quizCategoryRepository.save(quizCategory);
        return QuizCategoryDtoMapper.INSTANCE.toDetailResponse(quizCategory);
    }

    @Transactional(readOnly = true)
    public List<QuizCategoryDto.Response> getAll() {
        Restriction restriction = new Restriction();
        restriction.isNull("parent");
        return quizCategoryRepository.findAll(restriction.output()).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public QuizCategoryDto.DetailResponse get(@NonNull Long id) {
        QuizCategory quizCategory = quizCategoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Not found QuizCategory"));
        return this.toDetailResponse(quizCategory);
    }

    @Transactional
    public QuizCategoryDto.DetailResponse update(@NonNull Long id, @NonNull QuizCategoryDto.Update update) {
        QuizCategory quizCategory = quizCategoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Not found Quiz"));
        QuizCategoryDtoMapper.INSTANCE.merge(update, quizCategory);
        if (update.getParentId() != null) {
            QuizCategory parent = quizCategoryRepository.findById(update.getParentId()).orElseThrow(() -> new NotFoundException("Not found QuizCategory"));
            quizCategory.setParent(parent);
        }
        quizCategory = quizCategoryRepository.save(quizCategory);
        return this.toDetailResponse(quizCategory);
    }

    @Transactional
    public void delete(@NonNull Long id) {
        quizCategoryRepository.deleteById(id);
    }

    @Transactional
    public void deleteAll(@NonNull QuizCategoryDto.DeleteAll deleteAll) {
        quizCategoryRepository.deleteAllById(deleteAll.getIds());
    }

}
