package com.bite.friend.domain.user.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserExeResult {

    private String input; //输入

    private String expectOutput; // 预期输出

    private String output; // 实际输出

}
