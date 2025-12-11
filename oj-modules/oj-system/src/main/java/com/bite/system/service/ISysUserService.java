package com.bite.system.service;

import com.bite.common.core.domain.R;
import com.bite.common.core.domain.vo.LoginUserVO;
import com.bite.system.domain.dto.SysUserSaveDTO;

public interface ISysUserService {
    R<String> login(String userAccount, String password);

    /*
    添加用户
     */
    int add(SysUserSaveDTO sysUserSaveDTO);

    /*
    获取用户信息
     */
    R<LoginUserVO> info(String token);

    /*
    登出
     */
    boolean logout(String token);
}
