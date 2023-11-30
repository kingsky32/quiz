package com.seungju.quiz.app.room.domain;

import com.seungju.quiz.app.quiz.domain.QuizCategory;
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
@Table(name = "room_quiz")
public class RoomQuiz {
    @EmbeddedId
    private Id id;

    @MapsId("roomId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @MapsId("quizId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private QuizCategory quiz;

    @Getter
    @Setter
    @Embeddable
    public class Id implements Serializable {
        private static final long serialVersionUID = -273899381944893395L;
        @NotNull
        @Column(name = "room_id", nullable = false)
        private Long roomId;

        @NotNull
        @Column(name = "quiz_id", nullable = false)
        private Long quizId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
            Id entity = (Id) o;
            return Objects.equals(this.quizId, entity.quizId) &&
                    Objects.equals(this.roomId, entity.roomId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(quizId, roomId);
        }

    }


}
