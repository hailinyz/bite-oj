package com.bite.common.security.service;

import cn.hutool.core.lang.UUID;
import com.bite.common.core.constants.CacheConstants;
import com.bite.common.core.constants.JwtConstants;
import com.bite.common.redis.service.RedisService;
import com.bite.common.core.domain.LoginUser;
import com.bite.common.core.util.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//操作用户登录token的方法
@Service
@Slf4j
public class TokenService {

    @Autowired
    private RedisService redisService;


    public String createToken(Long userId, String secret,Integer identity,String nickName){

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
        String tokenkey = getTokenKey(userKey);
        LoginUser loginUser = new LoginUser();
        loginUser.setIdentity(identity);
        loginUser.setNickName(nickName);
        redisService.setCacheObject(tokenkey, loginUser,CacheConstants.EXP, TimeUnit.MINUTES);

        return token;

    }

    /*
    延长token有效期
    就是延长redis中存储的token有效期
    操作reids，拿到token中的userKey（唯一标识）
     */
    public void extendToken(String  token,String secret) { //在拦截器中判断token是否过期，过期就延长有效期
/*        Claims claims;
        try {
            claims = JwtUtils.parseToken(token, secret); //获取令牌中信息 解析payload中信息
            if (claims == null) {
                log.error("解析token：{}，出现异常",token);
                return;
            }
        } catch (Exception e) {
            log.error("解析token：{}，出现异常",token,e);
            return;
        }
        String userKey = JwtUtils.getUserKey(claims); //获取jwt中的key*/
        String userKey = getUserKey(token,secret);
        if (userKey == null) {
            return;
        }
        String tokenkey = getTokenKey(userKey);

        //720min 12h 剩余180min进行延长
        Long expire = redisService.getExpire(tokenkey, TimeUnit.MINUTES);

        if (expire != null && expire < CacheConstants.REFRESH_TIME ){
            redisService.expire(tokenkey, CacheConstants.EXP, TimeUnit.MINUTES);
        }

    }

    private String getTokenKey(String token) {
        return CacheConstants.LOGIN_TOKEN_KET + token;
    }

    private String getUserKey(String  token,String secret) {
        Claims claims;
        try {
            claims = JwtUtils.parseToken(token, secret); //获取令牌中信息 解析payload中信息
            if (claims == null) {
                log.error("解析token：{}，出现异常",token);
                return null;
            }
        } catch (Exception e) {
            log.error("解析token：{}，出现异常",token,e);
            return null;
        }
        return JwtUtils.getUserKey(claims); //获取jwt中的key
    }

    /*
    获取登录用户信息
     */
    public LoginUser getLoginUser(String token,String secret) {
        String userKey = getUserKey(token, secret);
        if (userKey == null){
            return null;
        }
        return redisService.getCacheObject(getTokenKey(userKey),LoginUser.class);
    }


    /*
    删除登录用户信息
     */
    public boolean deleteLoginUser(String token, String secret) {
        String userKey = getUserKey(token, secret);
        if (userKey == null){
            return false;
        }
        return redisService.deleteObject(getTokenKey(userKey));
    }




}
