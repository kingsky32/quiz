package com.seungju.quiz.restriction.repository;

import jakarta.persistence.criteria.JoinType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FetchJoin {
    public String fieldName;

    public JoinType joinType;

    public FetchJoin(String fieldName, JoinType joinType) {
        this.fieldName = fieldName;
        this.joinType = joinType;
    }
}
