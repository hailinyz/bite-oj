package com.bite.friend.service.user;

import com.bite.common.core.domain.TableDataInfo;
import com.bite.friend.domain.exam.dto.ExamDTO;
import com.bite.friend.domain.exam.dto.ExamQueryDTO;

public interface IUserExamService {


    int enter(String token, Long examId);


    TableDataInfo list(ExamQueryDTO examQueryDTO);
}
