package com.bite.friend.service.exam.impl;


import com.bite.friend.domain.exam.dto.ExamQueryDTO;
import com.bite.friend.domain.exam.vo.ExamVO;
import com.bite.friend.mapper.exam.examMapper;
import com.bite.friend.service.exam.IExamService;

import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamServiceImpl implements IExamService {

    @Autowired
    private examMapper examMapper;

    /*
     * 查询竞赛列表（老接口--> 未加入redis优化）
     */
    @Override
    public List<ExamVO> list(ExamQueryDTO examQueryDTO) {
        PageHelper.startPage(examQueryDTO.getPageNum(),examQueryDTO.getPageSize());
        return examMapper.selectExamList(examQueryDTO);
    }

    /*
     * 查询竞赛列表（新接口--> 加入了redis优化）
     */
    @Override
    public void redisList(ExamQueryDTO examQueryDTO) {

    }


}
