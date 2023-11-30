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
@Table(name = "room_quiz_category")
public class RoomQuizCategory {
    @EmbeddedId
    private Id id;

    @MapsId("roomId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @MapsId("quizCategoryId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_category_id", nullable = false)
    private QuizCategory quizCategory;

    @Getter
    @Setter
    @Embeddable
    public class Id implements Serializable {
        private static final long serialVersionUID = 6274436089941565935L;
        @NotNull
        @Column(name = "room_id", nullable = false)
        private Long roomId;

        @NotNull
        @Column(name = "quiz_category_id", nullable = false)
        private Long quizCategoryId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
            Id entity = (Id) o;
            return Objects.equals(this.quizCategoryId, entity.quizCategoryId) &&
                    Objects.equals(this.roomId, entity.roomId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(quizCategoryId, roomId);
        }

    }

}
