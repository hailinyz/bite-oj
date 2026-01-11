package com.bite.api;


import com.bite.api.domain.dto.JudgeSubmitDTO;
import com.bite.api.domain.vo.UserQuestionResultVO;
import com.bite.common.core.constants.Constants;
import com.bite.common.core.domain.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(contextId = "RemoteJudgeService", value = Constants.JUDGE_SERVICE)
public interface RemoteJudgeService { //Feign 远程调用接口

    @PostMapping("/judge/doJudgeJavaCode") //发送HTTP请求
    R<UserQuestionResultVO> doJudgeJavaCode(@RequestBody JudgeSubmitDTO judgeSubmitDTO);

}
