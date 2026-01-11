package com.bite.common.core.enums;

import lombok.Getter;

@Getter
public enum QuestionResult {
    PASS (1),
    ERROR (0);

    private Integer value;

    QuestionResult(Integer value) {
        this.value = value;
    }
}
