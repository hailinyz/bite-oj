package com.bite.system.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SysUserSaveDTO {

    @Schema(description = "用户账号") //数据模型属性描述
    private String userAccount;
    @Schema(description = "用户密码")
    private String password;

}
