package com.bite.friend.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bite.common.core.constants.CacheConstants;
import com.bite.common.core.constants.Constants;
import com.bite.common.core.enums.ResultCode;
import com.bite.common.core.enums.UserIdentity;
import com.bite.common.core.enums.UserStatus;
import com.bite.common.message.service.MockSmsService;
import com.bite.common.redis.service.RedisService;
import com.bite.common.security.exception.ServiceException;
import com.bite.common.security.service.TokenService;
import com.bite.friend.domain.User;
import com.bite.friend.domain.dto.UserDTO;
import com.bite.friend.mapper.UserMapper;
import com.bite.friend.service.IUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TokenService tokenService;

    @Value("${sms.code-expiration:5}") //获取nacos配置文件中的过期时间
    private Long phoneCodeExpiration; //手机验证码的过期时间

    @Value("${sms.send-limit:3}")
    private Integer sendLimit;

    @Value("${jwt.secret}") //获取nacos配置文件中的盐
    private String secret;

    @Value("${sms.is-send:false}")
    private boolean isSend; //开关打开：true 关闭：false

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
        String code = isSend ? RandomUtil.randomNumbers(6) : Constants.DEFAULT_CODE;
        //存储到redis中 数据结构：String key：p:c:手机号 value：code
        redisService.setCacheObject(phoneCodeKey, code, phoneCodeExpiration, TimeUnit.MINUTES);
        if (isSend){
            //发送验证码
            boolean sendMobileCode = mockSmsService.sendMobileCode(userDTO.getPhone(), code);
            if (!sendMobileCode){
                throw new ServiceException(ResultCode.FAILED_SEND_CODE);
            }
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
    登录注册
     */
    @Override
    public String codeLogin(String phone, String code) {
        //验证码的比对
        checkCode(phone, code);

        //判断新老用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (user == null){ //新用户
            //注册逻辑
            user = new User();
            user.setPhone(phone);
            user.setStatus(UserStatus.Normal.getValue());
            userMapper.insert(user);
        }

        //生成token
        return tokenService.createToken(user.getUserId(), secret, UserIdentity.ORDINARY.getValue(),user.getNickName());

/*        if (user != null){ //说明是老用户
            String phoneCodeKey = getPhoneCodeKey(phone);
            //获取redis中存储的验证码
            String cacheCode = redisService.getCacheObject(phoneCodeKey, String.class);
            //判断验证码是否是空
            if (StrUtil.isEmptyIfStr(cacheCode)){
                throw new ServiceException(ResultCode.FAILED_INVALID_CODE);
            }
            if (!cacheCode.equals(code)){
                throw new ServiceException(ResultCode.FAILED_ERROR_CODE);
            }
            //验证码已经比对成功，删除redis中的验证码
            redisService.deleteObject(phoneCodeKey);
            //生成token
            return tokenService.createToken(user.getUserId(), secret, UserIdentity.ORDINARY.getValue(),user.getNickName());
        }*/

        //新用户.在上面
    }



    /*
    验证码比对
     */
    private void checkCode(String phone, String code) {
        String phoneCodeKey = getPhoneCodeKey(phone);
        //获取redis中存储的验证码
        String cacheCode = redisService.getCacheObject(phoneCodeKey, String.class);
        //判断验证码是否是空
        if (StrUtil.isEmptyIfStr(cacheCode)){
            throw new ServiceException(ResultCode.FAILED_INVALID_CODE);
        }
        if (!cacheCode.equals(code)){
            throw new ServiceException(ResultCode.FAILED_ERROR_CODE);
        }
        //验证码已经比对成功，删除redis中的验证码
        redisService.deleteObject(phoneCodeKey);
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
