package com.bite.system.service.exam;

import com.bite.system.domain.exam.dto.ExamAddDTO;
import com.bite.system.domain.exam.dto.ExamEditDTO;
import com.bite.system.domain.exam.dto.ExamQueryDTO;
import com.bite.system.domain.exam.dto.ExamQuestionAddDTO;
import com.bite.system.domain.exam.vo.ExamDtailVO;
import com.bite.system.domain.exam.vo.ExamVO;
import com.bite.system.domain.question.vo.QuestionVO;

import java.util.List;

public interface ExamService {


    List<ExamVO> list(ExamQueryDTO examQueryDTO);

    String add(ExamAddDTO examAddDTO);

    boolean addQuestion(ExamQuestionAddDTO examQuestionAddDTO);

    ExamDtailVO detail(Long examId);

    int edit(ExamEditDTO examEditDTO);

    int questionDelete(Long examId, Long questionId);

    int delete(Long examId);
}
