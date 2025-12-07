package com.bite.common.core.util;

import com.bite.common.core.constants.JwtConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.HashMap;
import java.util.Map;

public class JwtUtils {
    /**
     * ⽣成令牌
     *
     * @param claims 数据
     * @param secret 密钥
     * @return 令牌
     */
    public static String createToken(Map<String, Object> claims, String secret)
    {
        String token =
                Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS512,
                        secret).compact();
        return token;
    }
    /**
     * 从令牌中获取数据
     *
     * @param token 令牌
     * @param secret 密钥
     * @return 数据
     */
    public static Claims parseToken(String token, String secret) {
        return
                Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    public static void main(String[] args) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", 123456789L);
        //secret 保密 随机 不能够硬编码（不能写源码中） 定期更换


        System.out.println(createToken(claims, "sdfghuijasxdjkawskuigy"));
    }

    public static String getUserKey(Claims claims) {
        Object value = claims.get(JwtConstants.LOGIN_USER_KRY);
        return toStr(value);
    }


    public static String getUserId(Claims claims) {
        Object value = claims.get(JwtConstants.LOGIN_USER_ID);
        return toStr(value);
    }


    private static String toStr(Object value) {
        if (value == null){
            return "";
        }
        return value.toString();
    }
}
