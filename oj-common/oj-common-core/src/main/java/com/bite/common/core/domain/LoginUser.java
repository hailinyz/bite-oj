package com.bite.common.core.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginUser {

    private Integer identity; //1:用户 2:管理员

    private String nickName; //用户昵称

    private String headImage; //头像

}
