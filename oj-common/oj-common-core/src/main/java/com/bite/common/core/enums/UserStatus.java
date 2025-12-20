package com.bite.common.core.enums;

public enum UserStatus {

    Normal(1),

    Block(0),

    ;

    private Integer value;

    UserStatus(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
