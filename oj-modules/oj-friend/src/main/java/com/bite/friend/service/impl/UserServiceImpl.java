package com.bite.friend.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.bite.common.core.constants.CacheConstants;
import com.bite.common.core.enums.ResultCode;
import com.bite.common.message.service.MockSmsService;
import com.bite.common.redis.service.RedisService;
import com.bite.common.security.exception.ServiceException;
import com.bite.friend.domain.dto.UserDTO;
import com.bite.friend.mapper.UserMapper;
import com.bite.friend.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private MockSmsService mockSmsService; //模拟发送短信服务, 用于测试,暂时不用阿里云的短信服务

    @Autowired
    private RedisService redisService;

    @Value("${sms.code-expiration:5}") //获取nacos配置文件中的过期时间
    private Long phoneCodeExpiration; //手机验证码的过期时间

    @Value("${sms.send-limit:3}")
    private Integer sendLimit;

    /*
    获取验证码
     */
    @Override
    public boolean sendCode(UserDTO userDTO) {
        //先判断传过来的是不是一个真的手机号
        if (!checkPhone(userDTO.getPhone())){
            throw new ServiceException(ResultCode.FAILED_USER_PHONE);
        }

        String phoneCodeKey =  getPhoneCodeKey(userDTO.getPhone());
        //获取redis中存储的验证码的过期 时间
        Long expire = redisService.getExpire(phoneCodeKey, TimeUnit.SECONDS);
        if (expire != null && (phoneCodeExpiration * 60 - expire) < 60){
            throw new ServiceException(ResultCode.FAILED_FREQUENT);
        }
        //每天的验证码不能超过50次，第二天清零 重新开始 计数
        //操作次数频繁，不需要存储，记录的次数当天有效 --> redis  String key： c:t:手机号
        //先获取已经请求的次数 和50 进行比较 如果大于50抛出异常，否则正常执行后续逻辑
        String codeTimeKey = getCodeTimeKey(userDTO.getPhone());
        Long sendTimes = redisService.getCacheObject(codeTimeKey, Long.class);
        if (sendTimes != null && sendTimes >= sendLimit ){
            throw new ServiceException(ResultCode.FAILED_TIME_LIMIT);
        }

        //生成验证码
        String code = RandomUtil.randomNumbers(6);
        //存储到redis中 数据结构：String key：p:c:手机号 value：code
        redisService.setCacheObject(phoneCodeKey, code, phoneCodeExpiration, TimeUnit.MINUTES);
        //发送验证码
        boolean sendMobileCode = mockSmsService.sendMobileCode(userDTO.getPhone(), code);
        if (!sendMobileCode){
            throw new ServiceException(ResultCode.FAILED_SEND_CODE);
        }
        //记录发送的次数
        redisService.increment(codeTimeKey);
        if (sendTimes == null){ //说明当天第一次发送获取验证码请求
            long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(),
                    LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0));
            //设置有效时间
            redisService.expire(codeTimeKey, seconds, TimeUnit.SECONDS);
        }
        return true;


    }



    /*
    判断手机号是否合法
     */
    public static boolean checkPhone(String phone) {
        // 中国手机号：1开头，第二位是3-9，后面9位数字
        Pattern regex = Pattern.compile("^1[3-9]\\d{9}$");
        Matcher m = regex.matcher(phone);
        return m.matches();
    }

    /*
    获取手机验证码的key
     */
    private String getPhoneCodeKey(String phone) {
        return CacheConstants.PHONE_CODE_KET + phone;
    }

    /*
    获取手机验证码的次数的key
     */
    private String getCodeTimeKey(String phone) {
        return CacheConstants.CODE_TIME_KET + phone;
    }


}
