package com.bite.system.domain.sysuser;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bite.common.core.domain.BaseEntity;
import lombok.*;

@TableName("tb_sys_user")
@Getter
@Setter
@ToString
public class SysUser extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long userId; //主键 不再使用aotu_increment
    private String userAccount;
    private String password;

    private String nickName;

}
