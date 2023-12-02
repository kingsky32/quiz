package com.seungju.quiz.app.room.domain;

import com.seungju.quiz.app.user.domain.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "room")
@EntityListeners(AuditingEntityListener.class)
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('READY', 'PLAYING', 'DELETED')", nullable = false)
    private Status status;

    @Size(max = 255)
    @NotNull
    @Column(name = "title", nullable = false)
    private String title;

    @NotNull
    @Column(name = "current_number", nullable = false)
    private Long currentNumber;

    @NotNull
    @Column(name = "number_of_quiz", nullable = false)
    private Long numberOfQuiz;

    @NotNull
    @Column(name = "is_secret", nullable = false)
    private Boolean isSecret = false;

    @Size(max = 255)
    @Column(name = "secret_password")
    private String secretPassword;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false, referencedColumnName = "id")
    @CreatedBy
    private User createdBy;

    @NotNull
    @Column(name = "created_at", nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "room")
    private List<RoomChat> roomChats = new ArrayList<>();

    @OneToMany(mappedBy = "room", fetch = FetchType.EAGER)
    private List<RoomQuizCategory> roomQuizCategories = new ArrayList<>();

    @OneToMany(mappedBy = "room", fetch = FetchType.EAGER)
    private List<RoomQuiz> roomQuizzes = new ArrayList<>();

    public enum Status {
        READY,
        PLAYING,
        DELETED,
    }
}
