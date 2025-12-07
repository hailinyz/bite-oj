package com.bite.system.controller;

import com.bite.common.core.controller.BaseController;
import com.bite.common.core.domain.R;
import com.bite.system.domain.dto.LoginDTO;
import com.bite.system.domain.dto.SysUserSaveDTO;
import com.bite.system.domain.vo.SysUserVO;
import com.bite.system.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sysUser")
@Tag(name = "管理员接口") //分组，类上
public class SysUserController extends BaseController {

    @Autowired
    private ISysUserService sysUserService;

    //至少要返回一个Message提示登录成功/失败把， bool true false || int code 1 成功 0 失败
    // 失败原因     String msg
    //因为返回两个字段，是不是一个对象啊，所以要定义一个对象
    //请求方法Get还是.... 和 URL  sysuser/lo gin
    //接口文档  统一的响应数据的结构
    @PostMapping("/login") //登录安全考虑，使用Post
    @Operation(summary = "管理员登录",description = "根据账号密码进行管理员登录") //操作描述方法
    @ApiResponse(responseCode = "1000",description = "操作成功") //响应码描述方法
    @ApiResponse(responseCode = "2000",description = "服务繁忙请稍后重试")
    @ApiResponse(responseCode = "3102",description = "用户不存在")
    @ApiResponse(responseCode = "3103",description = "用户名或密码错误")
    public R<String> login(@RequestBody LoginDTO loginDTO){
        return sysUserService.login(loginDTO.getUserAccount(), loginDTO.getPassword());
    }

    @PostMapping("/add")
    @Operation(summary = "新增管理员",description = "根据提供的信息新增管理员") //操作描述方法
    @ApiResponse(responseCode = "1000",description = "操作成功") //响应码描述方法
    @ApiResponse(responseCode = "2000",description = "服务繁忙请稍后重试")
    @ApiResponse(responseCode = "3101",description = "用户已存在")
    public R<Void> add(@RequestBody SysUserSaveDTO sysUserSaveDTO){
        return toR(sysUserService.add(sysUserSaveDTO));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "删除用户", description = "通过用户id删除用户")
    @Parameters(value = {
            @Parameter(name = "userId", in = ParameterIn.PATH, description = "用户ID")
    })
    @ApiResponse(responseCode = "1000", description = "成功删除用户")
    @ApiResponse(responseCode = "2000", description = "服务繁忙请稍后重试")
    @ApiResponse(responseCode = "3101", description = "用户不存在")
    public R<Void> delete(@PathVariable Long userId) {
        return null;
    }

    //修改我就不演示了和新增差不多
    @Operation(summary = "用户详情", description = "根据查询条件查询用户详情")
    @GetMapping("/detail")
    @Parameters(value = {
            @Parameter(name = "userId", in = ParameterIn.QUERY, description = "用户ID"),
            @Parameter(name = "sex", in = ParameterIn.QUERY, description = "用户性别")
    })
    @ApiResponse(responseCode = "1000", description = "成功获取用户信息")
    @ApiResponse(responseCode = "2000", description = "服务繁忙请稍后重试")
    @ApiResponse(responseCode = "3101", description = "用户不存在")
    public R<SysUserVO> detail(@RequestParam(required = true) Long userId, @RequestParam(required = false) String sex) {
        return null;
    }


}
