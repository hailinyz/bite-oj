package com.bite.system.service.question;


import com.bite.system.domain.question.dto.QuestionQueryDTO;
import com.bite.system.domain.question.vo.QuestionVO;

import java.util.List;

public interface IQuestionService {

    /*
    * 获取题目列表接口
     */
    List<QuestionVO> list(QuestionQueryDTO questionQueryDTO);

}
