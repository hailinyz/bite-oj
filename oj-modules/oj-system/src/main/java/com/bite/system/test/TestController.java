package com.bite.system.test;

import com.bite.common.core.domain.R;
import com.bite.common.core.enums.ResultCode;
import com.bite.system.test.service.ITestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private ITestService testService;

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
}
