package com.bite.system.service;

import com.bite.common.core.domain.R;
import com.bite.system.controller.LoginResult;

public interface ISysUserService {
    R<String> login(String userAccount, String password);
}
