package com.seungju.quiz.app.room.domain;

import com.seungju.quiz.app.quiz.domain.Quiz;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "room_quiz")
@EntityListeners(AuditingEntityListener.class)
public class RoomQuiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @OneToMany(mappedBy = "roomQuiz")
    private List<RoomChatAnswer> roomChatAnswers = new ArrayList<>();

    @NotNull
    @Column(name = "created_at", nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;

}
