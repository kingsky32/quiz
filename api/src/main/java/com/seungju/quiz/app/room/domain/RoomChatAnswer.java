package com.seungju.quiz.app.room.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "room_chat_answer")
public class RoomChatAnswer {
    @EmbeddedId
    private Id id;

    @MapsId("roomChatId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_chat_id", nullable = false)
    private RoomChat roomChat;

    @MapsId("quizId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_quiz_id", nullable = false)
    private RoomQuiz roomQuiz;

    @Getter
    @Setter
    @Embeddable
    public static class Id implements Serializable {
        private static final long serialVersionUID = -91243310727980717L;
        @NotNull
        @Column(name = "room_chat_id", nullable = false)
        private Long roomChatId;

        @NotNull
        @Column(name = "room_quiz_id", nullable = false)
        private Long roomQuizId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
            Id entity = (Id) o;
            return Objects.equals(this.roomQuizId, entity.roomQuizId) &&
                    Objects.equals(this.roomChatId, entity.roomChatId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(roomQuizId, roomChatId);
        }

    }

}
