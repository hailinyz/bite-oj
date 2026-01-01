package com.bite.friend.controller;

import com.bite.common.core.constants.HttpConstants;
import com.bite.common.core.controller.BaseController;
import com.bite.common.core.domain.R;
import com.bite.friend.domain.dto.UserDTO;
import com.bite.friend.service.IUserService;
import com.bite.system.domain.sysuser.vo.LoginUserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
         return toR(userService.sendCode(userDTO));
    }

    /*
    登录注册
     */
    @PostMapping("code/login")
    public R<String> codeLogin(@RequestBody UserDTO userDTO) {
        return R.ok(userService.codeLogin(userDTO.getPhone(), userDTO.getCode()));
    }

    /*
    退出登录
     */
    @DeleteMapping("/logout")
    public R<Void> logout(@RequestHeader(HttpConstants.AUTHENTICATION) String token){
        return toR(userService.logout(token)) ;
    }

    /*
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public R<LoginUserVO> info(@RequestHeader(HttpConstants.AUTHENTICATION) String token){
        return userService.info(token);
    }

}
