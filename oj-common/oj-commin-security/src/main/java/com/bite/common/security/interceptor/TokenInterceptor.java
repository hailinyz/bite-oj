package com.bite.common.security.interceptor;

import cn.hutool.core.util.StrUtil;
import com.bite.common.core.constants.Constants;
import com.bite.common.core.constants.HttpConstants;
import com.bite.common.core.util.ThreadLocalUtil;
import com.bite.common.security.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private TokenService tokenService;

    @Value("${jwt.secret}")
    private String secret;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = getToken(request); //从请求头中获取token
        if (StrUtil.isEmpty(token)) {
            return true;
        }
        Long userId = tokenService.getUserId(token, secret);
        //使用ThreadLocal保存用户ID，方便后续使用
        ThreadLocalUtil.set(Constants.USER_ID, userId);
        tokenService.extendToken(token,secret);
        return true;
    }

    /*
    从请求头中获取token
     */
    private String getToken(HttpServletRequest request) {
        String token = request.getHeader(HttpConstants.AUTHENTICATION);
        if (StrUtil.isNotEmpty(token) && token.startsWith(HttpConstants.PREFIX)) {
            token = token.replaceFirst(HttpConstants.PREFIX, "");
        }
        return token;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        ThreadLocalUtil.remove(); // 清空ThreadLocal里面的数据清理掉
    }

}
