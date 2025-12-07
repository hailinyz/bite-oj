package com.bite.system.test;

import com.bite.common.core.domain.R;
import com.bite.common.core.enums.ResultCode;
import com.bite.common.redis.service.RedisService;
import com.bite.system.domain.SysUser;
import com.bite.system.test.service.ITestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {

    @Autowired
    private ITestService testService;

    @Autowired
    private RedisService redisService;


    //test/list 查询tb_test所有数据
    @GetMapping("/list")
    public List<?> list(){
        return testService.list();
    }

    @GetMapping("/add")
    public String add(){
        return testService.add();
    }

    @GetMapping("apifoxtest")
    public R<String> apifoxtest(String apiId){
        R<String> result = new R<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMsg(ResultCode.SUCCESS.getMsg());
        result.setData("apifoxtest:" + apiId);
        return result;
    }

    @GetMapping("/redisAddAndGet")
    public String redisAddAndGet(){
        SysUser sysUser = new SysUser();
        sysUser.setUserAccount("redisTest");
        redisService.setCacheObject("u", sysUser);

        SysUser us = redisService.getCacheObject("u", SysUser.class);

        return us.toString();

    }

    @GetMapping("/log")
    public String log(){
        log.info("我是info级日志");
        log.error("我是error级日志");
        return "日志测试";
    }
}
