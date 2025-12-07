package com.bite.common.security.service;


import cn.hutool.core.lang.UUID;
import com.bite.common.core.constants.CacheConstants;
import com.bite.common.core.constants.JwtConstants;
import com.bite.common.redis.service.RedisService;
import com.bite.common.core.domain.LoginUser;
import com.bite.common.core.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//操作用户登录token的方法
@Service
public class TokenService {

    @Autowired
    private RedisService redisService;


    public String createToken(Long userId, String secret,Integer identity){

        Map<String, Object> claims = new HashMap<>();
        String userKey = UUID.fastUUID().toString();
        claims.put(JwtConstants.LOGIN_USER_ID, userId);
        claims.put(JwtConstants.LOGIN_USER_KRY, userKey);
        String token = JwtUtils.createToken(claims, secret);

        //第三方中存放敏感信息
        //身份认证具体存储的信息，redis 表明用户身份字段 identity 1:用户 2:管理员 对象好扩展一点LoginUser
        // 使用啥样的数据结构 String hash list set zset
        // key必须唯一，便于维护  统一前缀：logintoken:userId 是通过雪花生成所以唯一
        //也可以用糊涂工具生成UUID作为唯一标识，跟前缀拼接实现唯一
        // ，过期时间咋记录，定多长  720分钟
        String key = CacheConstants.LOGIN_TOKEN_KET + userKey;
        LoginUser loginUser = new LoginUser();
        loginUser.setIdentity(identity);
        redisService.setCacheObject(key, loginUser,CacheConstants.EXP, TimeUnit.MINUTES);

        return token;

    }

}
