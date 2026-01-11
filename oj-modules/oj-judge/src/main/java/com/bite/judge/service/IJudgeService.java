package com.bite.judge.service;

import com.bite.api.domain.dto.JudgeSubmitDTO;
import com.bite.api.domain.vo.UserQuestionResultVO;

public interface IJudgeService {
    UserQuestionResultVO doJudgeJavaCode(JudgeSubmitDTO judgeSubmitDTO);
}
