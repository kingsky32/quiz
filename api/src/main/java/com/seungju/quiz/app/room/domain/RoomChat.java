package com.seungju.quiz.app.room.domain;

import com.seungju.quiz.app.user.domain.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "room_chat")
@EntityListeners(AuditingEntityListener.class)
public class RoomChat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false, referencedColumnName = "id")
    private User createdBy;

    @NotNull
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    @CreatedBy
    private String content;

    @NotNull
    @Column(name = "created_at", nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;

}
