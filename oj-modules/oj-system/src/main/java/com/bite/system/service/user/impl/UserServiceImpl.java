package com.bite.system.service.user.impl;

import com.bite.common.core.enums.ResultCode;
import com.bite.common.security.exception.ServiceException;
import com.bite.system.domain.user.User;
import com.bite.system.domain.user.dto.UserDTO;
import com.bite.system.domain.user.dto.UserQueryDTO;
import com.bite.system.domain.user.vo.UserVO;
import com.bite.system.mapper.user.UserMapper;
import com.bite.system.service.user.IUserService;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    /*
    获取用户列表
     */
    @Override
    public List<UserVO> list(UserQueryDTO userQueryDTO) {
        PageHelper.startPage(userQueryDTO.getPageNum(),userQueryDTO.getPageSize());
        return userMapper.selectUserList(userQueryDTO);
    }

    /*
    修改用户状态
     */
    @Override
    public int updateStatus(UserDTO userDTO) {
        User user = userMapper.selectById(userDTO.getUserId());
        if (user == null){
            throw new ServiceException(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        user.setStatus(userDTO.getStatus());
        return userMapper.updateById(user);
    }


}
