package com.seungju.quiz.restriction.repository;

import com.seungju.quiz.restriction.constant.ConditionType;
import lombok.Data;

@Data
public class Condition {
    private ConditionType type;
    private Object name;
    private Object value1;
    private Object value2;

    public Condition(ConditionType type, Object name) {
        this(type, name, null);
    }

    public Condition(ConditionType type, Object name, Object value1) {
        this.type = type;
        this.name = name;
        this.value1 = value1;
    }

    public Condition(ConditionType type, Object name, Object value1, Object value2) {
        this.type = type;
        this.name = name;
        this.value1 = value1;
        this.value2 = value2;
    }

}
