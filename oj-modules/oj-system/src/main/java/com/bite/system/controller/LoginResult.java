package com.bite.system.controller;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResult {

    private int code;  // 0 登录成功  1 登录失败

    private String msg; // 登录失败的原因
}
