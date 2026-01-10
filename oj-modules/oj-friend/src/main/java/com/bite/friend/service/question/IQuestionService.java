package com.bite.friend.service.question;

import com.bite.common.core.domain.TableDataInfo;
import com.bite.friend.domain.question.dto.QuestionQueryDTO;
import com.bite.friend.domain.question.vo.QuestionDetailVO;

public interface IQuestionService {

    TableDataInfo list(QuestionQueryDTO questionQueeryDTO);

    QuestionDetailVO detail(Long questionId);
}
