package com.bite.friend.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.bite.common.core.enums.ResultCode;
import com.bite.common.security.exception.ServiceException;
import com.bite.friend.domain.dto.UserDTO;
import com.bite.friend.mapper.UserMapper;
import com.bite.friend.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    /*
    获取验证码
     */
    @Override
    public void sendCode(UserDTO userDTO) {
        //先判断传过来的是不是一个真的手机号
        if (!checkPhone(userDTO.getPhone())){
            throw new ServiceException(ResultCode.FAILED_USER_PHONE);
        }
        //生成验证码
        String code = RandomUtil.randomNumbers(6);

    }

    /*
    判断手机号是否合法
     */
    public static boolean checkPhone(String phone) {
        Pattern regex = Pattern.compile("^[2|3|4|5|6|7|8|9][0-9]\\d{8}$");
        Matcher m = regex.matcher(phone);
        return m.matches();
    }
}
