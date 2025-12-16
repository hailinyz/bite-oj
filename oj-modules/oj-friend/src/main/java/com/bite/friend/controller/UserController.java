package com.bite.friend.controller;

import com.bite.common.core.controller.BaseController;
import com.bite.common.core.domain.R;
import com.bite.friend.domain.dto.UserDTO;
import com.bite.friend.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    @Autowired
    private IUserService userService;

    /*
    获取验证码
     */
    @PostMapping("/sendCode")
    //返回值使用Void，是因为无需给前端，给手机，为了安全
    public R<Void> sendCode(@RequestBody UserDTO userDTO) { //因为传的是手机号，为了避免被XSS攻击，使用DTO
         userService.sendCode(userDTO);
    }

}
