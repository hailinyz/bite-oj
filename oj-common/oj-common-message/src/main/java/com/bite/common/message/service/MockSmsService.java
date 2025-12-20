package com.bite.common.message.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 模拟短信服务 - 用于开发测试
 * 不真正发送短信，只打印日志
 */
@Component
@Slf4j
public class MockSmsService implements SmsService {

    /**
     * 模拟发送短信验证码
     */
    public boolean sendMobileCode(String phone, String code) {
        log.info("【模拟短信】发送验证码到手机号: {}, 验证码: {}", phone, code);
        log.info("【模拟短信】短信内容: 您的验证码为：{}，该验证码5分钟内有效", code);
        // 模拟发送成功
        return true;
    }

    /**
     * 模拟发送模板消息
     */
    public boolean sendTempMessage(String phone, String signName, String templateCode,
                                   Map<String, String> params) {
        log.info("【模拟短信】发送模板消息");
        log.info("  手机号: {}", phone);
        log.info("  签名: {}", signName);
        log.info("  模板CODE: {}", templateCode);
        log.info("  参数: {}", params);
        // 模拟发送成功
        return true;
    }
}
