package com.bite.friend.service;

import com.bite.common.core.domain.R;
import com.bite.friend.domain.dto.UserDTO;
import com.bite.system.domain.sysuser.vo.LoginUserVO;

public interface IUserService {

    boolean sendCode(UserDTO userDTO);

    String codeLogin(String phone, String code);

    boolean logout(String token);

    R<LoginUserVO> info(String token);

}
