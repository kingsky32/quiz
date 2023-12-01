package com.seungju.quiz.app.quiz.domain;

import com.seungju.quiz.app.file.domain.File;
import com.seungju.quiz.app.room.domain.RoomQuiz;
import com.seungju.quiz.app.user.domain.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "quiz")
@EntityListeners(AuditingEntityListener.class)
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_category_id", nullable = false)
    private QuizCategory quizCategory;

    @Size(max = 255)
    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sound_file_id")
    private File soundFile;

    @NotNull
    @Column(name = "timeout_ms", nullable = false)
    private Long timeoutMs;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_id")
    @CreatedBy
    private User createdBy;

    @NotNull
    @Column(name = "created_at", nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @NotNull
    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "quiz", fetch = FetchType.EAGER)
    private List<QuizAnswer> quizAnswers = new ArrayList<>();

    @OneToMany(mappedBy = "quiz", fetch = FetchType.EAGER)
    private List<QuizHint> quizHints = new ArrayList<>();

    @OneToMany(mappedBy = "quiz")
    private List<RoomQuiz> roomQuizzes = new ArrayList<>();

}
