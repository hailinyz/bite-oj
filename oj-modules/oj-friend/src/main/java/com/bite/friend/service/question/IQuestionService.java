package com.bite.friend.service.question;

import com.bite.common.core.domain.TableDataInfo;
import com.bite.friend.domain.question.dto.QuestionQueryDTO;

public interface IQuestionService {

    TableDataInfo list(QuestionQueryDTO questionQueeryDTO);

}
