package com.bite.common.core.enums;

import lombok.Getter;

@Getter
public enum ProgramType {
    JAVA (0, "java语言"),
    CPP (1, "c++语言"),
    GOLANG (2, "golang语言"),;

    private Integer value;

    private String description;

    ProgramType(Integer value, String description) {
        this.value = value;
        this.description = description;
    }
}
