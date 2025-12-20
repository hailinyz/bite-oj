package com.bite.common.message.service;

import java.util.Map;

/**
 * 短信服务接口
 */
public interface SmsService {
    
    /**
     * 发送短信验证码
     */
    boolean sendMobileCode(String phone, String code);
    
    /**
     * 发送模板消息
     */
    boolean sendTempMessage(String phone, String signName, String templateCode,
                           Map<String, String> params);
}
