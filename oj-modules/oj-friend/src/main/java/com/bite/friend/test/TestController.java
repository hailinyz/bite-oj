package com.bite.friend.test;

import com.bite.common.core.controller.BaseController;
import com.bite.common.core.domain.R;
import com.bite.common.message.service.MockSmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController extends BaseController {

    @Autowired
    private MockSmsService mockSmsService;  // 使用模拟短信服务进行测试

    @GetMapping("/sendCode")
    public R<Void> sendCode(String  phone, String code){
        return toR(mockSmsService.sendMobileCode(phone, code));
    }

}
