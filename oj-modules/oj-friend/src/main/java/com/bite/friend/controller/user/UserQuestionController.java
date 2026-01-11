package com.bite.friend.controller.user;

import com.bite.api.domain.vo.UserQuestionResultVO;
import com.bite.common.core.controller.BaseController;
import com.bite.common.core.domain.R;
import com.bite.friend.domain.user.dto.UserSubmitDTO;
import com.bite.friend.service.user.IUserQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/question")
public class UserQuestionController extends BaseController {

    @Autowired
    private IUserQuestionService userQuestionService;

    /*
    接收用户代码提交 --> 判题 --> 返回结果
     */
    @PostMapping("/submit")
    public R<UserQuestionResultVO> submit(@RequestBody UserSubmitDTO userSubmitDTO){
        return userQuestionService.submit(userSubmitDTO);
    }



}
