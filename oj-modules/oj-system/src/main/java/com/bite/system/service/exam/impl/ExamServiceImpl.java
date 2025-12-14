package com.bite.system.service.exam.impl;

import com.bite.system.domain.exam.dto.ExamQueryDTO;
import com.bite.system.domain.exam.vo.ExamVO;
import com.bite.system.mapper.exam.examMapper;
import com.bite.system.service.exam.ExamService;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamServiceImpl implements ExamService {

    @Autowired
    private examMapper examMapper;

    /*
    * 查询竞赛列表接口
     */
    @Override
    public List<ExamVO> list(ExamQueryDTO examQueryDTO) {
        PageHelper.startPage(examQueryDTO.getPageNum(),examQueryDTO.getPageSize());
        return examMapper.selectExamList(examQueryDTO);
    }



}
