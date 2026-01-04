package com.bite.friend.service.exam.impl;


import cn.hutool.core.collection.CollectionUtil;
import com.bite.common.core.domain.TableDataInfo;
import com.bite.common.redis.service.RedisService;
import com.bite.friend.domain.exam.dto.ExamQueryDTO;
import com.bite.friend.domain.exam.vo.ExamVO;
import com.bite.friend.manger.ExamCacheManager;
import com.bite.friend.mapper.exam.ExamMapper;
import com.bite.friend.service.exam.IExamService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamServiceImpl implements IExamService {

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private ExamCacheManager examCacheManager;

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
    public TableDataInfo redisList(ExamQueryDTO examQueryDTO) {
        // 如果有时间过滤条件，直接查数据库
        if (examQueryDTO.getStartTime() != null || examQueryDTO.getEndTime() != null) {
            List<ExamVO> examVOList = list(examQueryDTO);
            long total = new PageInfo<>(examVOList).getTotal();
            return TableDataInfo.success(examVOList, total);
        }

        // 没有时间过滤，走缓存逻辑
        //从redis中获取 竞赛列表数据
        Long total = examCacheManager.getListSize(examQueryDTO.getType());
        List<ExamVO> examVOList;
        if (total == null || total <= 0){
            //从数据库中获取 竞赛列表数据
            examVOList = list(examQueryDTO);
            //同步到redis中
            examCacheManager.refreshCache(examQueryDTO.getType());
            total = new PageInfo<>(examVOList).getTotal(); //获取总记录数
        } else {
            //从redis中获取 竞赛列表数据
            examVOList = examCacheManager.getExamVOList(examQueryDTO);
            total =  examCacheManager.getListSize(examQueryDTO.getType()); // 获取总记录数
        }
        if (CollectionUtil.isEmpty(examVOList)){ //使用hutool工具包判断集合是否为空
            return TableDataInfo.empty(); //未查出任何数据时调用
        }
        return TableDataInfo.success(examVOList, total);
    }



}
