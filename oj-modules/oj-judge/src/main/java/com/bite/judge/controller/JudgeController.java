package com.bite.judge.controller;

import com.bite.api.domain.dto.JudgeSubmitDTO;
import com.bite.api.domain.vo.UserQuestionResultVO;
import com.bite.common.core.controller.BaseController;
import com.bite.common.core.domain.R;
import com.bite.judge.service.IJudgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/judge")
public class JudgeController extends BaseController {

    @Autowired
    private IJudgeService judgeService;


    /*
    判题接口
     */
    @PostMapping("/doJudgeJavaCode")
    public R<UserQuestionResultVO> doJudgeJavaCode(@RequestBody JudgeSubmitDTO judgeSubmitDTO) {
        return R.ok(judgeService.doJudgeJavaCode(judgeSubmitDTO));
    }


}
