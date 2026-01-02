package com.bite.system.manger;


import com.bite.common.core.constants.CacheConstants;
import com.bite.common.redis.service.RedisService;
import com.bite.system.domain.exam.Exam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExamCacheManager {


    @Autowired
    private RedisService redisService;

    public void addCache(Exam exam) {
        redisService.leftPushForList(getExamListKey(), exam.getExamId()); // 添加到未完成列表
        redisService.setCacheObject(getDetailKey(exam.getExamId()), exam); // 添加到缓存中
    }

    public void deleteCache(Long examId) {
        redisService.removeForList(getExamListKey(), examId); // 删除未完成列表
        redisService.deleteObject(getDetailKey(examId)); // 删除缓存中
    }


    private String getExamListKey() {
        return CacheConstants.EXAM_UNFINISHED_LIST;
    }


    private String getDetailKey(Long examId) {
        return CacheConstants.EXAM_DETAIL + examId;
    }



}
