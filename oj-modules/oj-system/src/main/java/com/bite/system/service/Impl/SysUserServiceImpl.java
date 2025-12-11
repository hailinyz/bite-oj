package com.bite.system.service.Impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bite.common.core.constants.HttpConstants;
import com.bite.common.core.domain.LoginUser;
import com.bite.common.core.domain.R;
import com.bite.common.core.domain.vo.LoginUserVO;
import com.bite.common.core.enums.ResultCode;
import com.bite.common.core.enums.UserIdentity;
import com.bite.common.security.exception.ServiceException;
import com.bite.common.security.service.TokenService;
import com.bite.system.domain.SysUser;
import com.bite.system.domain.dto.SysUserSaveDTO;
import com.bite.system.mapper.SysUserMapper;
import com.bite.system.service.ISysUserService;
import com.bite.system.utils.BCryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RefreshScope // 动态刷新，当更新配置文件时，会自动更新无需重启
public class SysUserServiceImpl implements ISysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private TokenService tokenService;

    @Value("${jwt.secret}")
    private String secret;

    @Override
    public R<String> login(String userAccount, String password) {
        //通过账号去数据库查询对应的用户信息
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        // select password from tb_sys_user where user_account = #{userAccount}
        SysUser sysUser = sysUserMapper.selectOne(queryWrapper
                .select(SysUser::getUserId, SysUser::getPassword,SysUser::getNickName)
                .eq(SysUser::getUserAccount, userAccount));

/*        R loginResult = new R();*/
        if (sysUser == null){
/*            loginResult.setCode(ResultCode.FAILED_USER_NOT_EXISTS.getCode());
            loginResult.setMsg(ResultCode.FAILED_USER_NOT_EXISTS.getMsg());
            return loginResult;*/
            return R.fail(ResultCode.FAILED_USER_NOT_EXISTS);
        }
        if (BCryptUtils.matchesPassword(password,sysUser.getPassword())){
/*            loginResult.setCode(ResultCode.SUCCESS.getCode());
            loginResult.setMsg(ResultCode.SUCCESS.getMsg());
            return loginResult;*/

            // jwttoken = 生成jwttoken的方法
            String token = tokenService.createToken(sysUser.getUserId(),
                    secret, UserIdentity.ADMIN.getValue(),sysUser.getNickName());
            return R.ok(token);
        }
/*        loginResult.setCode(ResultCode.FAILED_LOGIN.getCode());
        loginResult.setMsg(ResultCode.FAILED_LOGIN.getMsg());
        return loginResult;*/
        return R.fail(ResultCode.FAILED_LOGIN);
    }

    /*
    * 添加用户
     */
    @Override
    public int add(SysUserSaveDTO sysUserSaveDTO) {

//        checkParams(sysUserSaveDTO);

        //将DTO对象转换成实体对象
        List<SysUser> sysUserList = sysUserMapper.selectList(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserAccount, sysUserSaveDTO.getUserAccount()));
        if (CollectionUtil.isNotEmpty(sysUserList)){
            //账号已存在
            //自定义异常 公共异常类
            throw new ServiceException(ResultCode.AILED_USER_EXISTS);
        }
        SysUser sysUser = new SysUser();
        sysUser.setUserAccount(sysUserSaveDTO.getUserAccount());
        sysUser.setPassword(BCryptUtils.encryptPassword(sysUserSaveDTO.getPassword()));
        return sysUserMapper.insert(sysUser);
    }

    /*
    * 获取用户信息
     */
    @Override
    public R<LoginUserVO> info(String token) {
        if (StrUtil.isNotEmpty(token) &&
                token.startsWith(HttpConstants.PREFIX)) {
            token = token.replaceFirst(HttpConstants.PREFIX, StrUtil.EMPTY);
        }
        LoginUser loginUser = tokenService.getLoginUser(token, secret);
        if (loginUser == null){
            return R.fail();
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setNickName(loginUser.getNickName());
        return R.ok(loginUserVO);
    }

    /*
    * 登出
     */
    @Override
    public boolean logout(String token) {
        if (StrUtil.isNotEmpty(token) &&
                token.startsWith(HttpConstants.PREFIX)) {
            token = token.replaceFirst(HttpConstants.PREFIX, StrUtil.EMPTY);
        }
        return tokenService.deleteLoginUser(token, secret);
    }

/*    private void checkParams(SysUserSaveDTO sysUserSaveDTO){
        if (sysUserSaveDTO == null){
            throw new ServiceException();
        }
    }*/


}
