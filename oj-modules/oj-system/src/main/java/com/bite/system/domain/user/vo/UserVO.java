package com.bite.system.domain.user.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;

//VO里面放什么取决于页面上展示什么
@Getter
@Setter
public class UserVO {

    @JsonSerialize(using = ToStringSerializer.class)//为了防止截断加注解
    private Long userId;

    private String nickName;

    private Integer sex;

    private String phone;

    private String email;

    private String wechat;

    private String schoolName;

    private String majorName;

    private String introduce;

    private Integer status;

}
