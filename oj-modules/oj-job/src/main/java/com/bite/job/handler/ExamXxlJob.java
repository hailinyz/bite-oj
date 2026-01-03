package com.bite.job.handler;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bite.common.core.constants.CacheConstants;
import com.bite.common.core.constants.Constants;
import com.bite.common.core.enums.ExamListType;
import com.bite.common.redis.service.RedisService;
import com.bite.job.domain.exam.Exam;
import com.bite.job.mapper.exam.ExamMapper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ExamXxlJob {

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private RedisService redisService;


    @XxlJob("examListOrganizeHandler")
    public void examListOrganizeHandler() {
        // 统计哪些竞赛应该存入未完赛列表中  //哪些竞赛应该存入历史竞赛列表中  统计出来之后，存入对应的缓存中
        log.info("*** 开始执行定时任务examListOrganizeHandler ***");
        List<Exam> unFinishList = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                .select(Exam::getExamId, Exam::getTitle, Exam::getStartTime, Exam::getEndTime)
                .gt(Exam::getEndTime, LocalDateTime.now())
                .eq(Exam::getStatus, Constants.TRUE)
                .orderByDesc(Exam::getCreateTime));

        refreshCache(unFinishList, CacheConstants.EXAM_UNFINISHED_LIST);

        List<Exam> historyList = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                .select(Exam::getExamId, Exam::getTitle, Exam::getStartTime, Exam::getEndTime)
                .le(Exam::getEndTime, LocalDateTime.now())
                .eq(Exam::getStatus, Constants.TRUE)
                .orderByDesc(Exam::getCreateTime));

        refreshCache(historyList, CacheConstants.EXAM_HISTORY_LIST);


    }

    public void refreshCache(List<Exam> examList, String examListKey) {
            if (CollectionUtil.isEmpty(examList)) {
            return;
        }

        Map<String, Exam> examMap = new HashMap<>();
        List<Long> examIdList = new ArrayList<>();
            for (Exam exam : examList) {
            examMap.put(getDetailKey(exam.getExamId()), exam);
            examIdList.add(exam.getExamId());
        }
            redisService.multiSet(examMap); //刷新详情缓存
            redisService.deleteObject(examListKey);
            redisService.rightPushAll(examListKey, examIdList); //刷新列表缓存
    }


    private String getDetailKey(Long examId) {
        return CacheConstants.EXAM_DETAIL + examId;
    }



}
