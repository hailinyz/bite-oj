package com.bite.friend.service.user;

import com.bite.friend.domain.exam.dto.ExamDTO;

public interface IUserExamService {


    int enter(String token, Long examId);


}
