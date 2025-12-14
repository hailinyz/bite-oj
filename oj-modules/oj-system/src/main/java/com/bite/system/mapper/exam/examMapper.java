package com.bite.system.mapper.exam;



import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bite.system.domain.exam.Exam;
import com.bite.system.domain.exam.dto.ExamQueryDTO;
import com.bite.system.domain.exam.vo.ExamVO;

import java.util.List;

public interface examMapper extends BaseMapper<Exam> {

    List<ExamVO> selectExamList(ExamQueryDTO examQueryDTO);
}
