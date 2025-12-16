package com.bite.friend.service;

import com.bite.friend.domain.dto.UserDTO;

public interface IUserService {
    void sendCode(UserDTO userDTO);
}
