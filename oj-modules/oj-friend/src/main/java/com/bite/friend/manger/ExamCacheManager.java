package com.bite.friend.manger;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bite.common.core.constants.CacheConstants;
import com.bite.common.core.constants.Constants;
import com.bite.common.core.enums.ExamListType;
import com.bite.common.redis.service.RedisService;
import com.bite.friend.domain.exam.Exam;
import com.bite.friend.domain.exam.dto.ExamQueryDTO;
import com.bite.friend.domain.exam.vo.ExamVO;
import com.bite.friend.mapper.exam.ExamMapper;
import com.bite.friend.mapper.user.UserExamMapper;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExamCacheManager {

    @Autowired
    private RedisService redisService;

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private UserExamMapper userExamMapper;

    /*
    将用户竞赛信息存储到 redis中
     */
    public void addUserExamCache(Long  userId, Long  examId){
        String userExamListKey = getUserExamListKey(userId);
        redisService.leftPushForList( userExamListKey, examId);
    }


    public Long getListSize(Integer examListType, Long  userId) {
        String examListKey = getExamListKey(examListType,  userId);
        return redisService.getListSize(examListKey);
    }


    public List<ExamVO> getExamVOList(ExamQueryDTO examQueryDTO,  Long userId) {
        int start = (examQueryDTO.getPageNum() - 1) * examQueryDTO.getPageSize();
        int end = start + examQueryDTO.getPageSize() - 1; //下标需要-1
        String examListKey = getExamListKey(examQueryDTO.getType(),   userId);
        List<Long> examIdList = redisService.getCacheListByRange(examListKey, start, end, Long.class);
        List<ExamVO> examVOList = assembleExamVOList(examIdList);
        if (CollectionUtil.isEmpty(examVOList)) {
            //说明redis中数据可能有问题 从数据库中查数据并且重新刷新缓存
            examVOList = getExamListByDB(examQueryDTO,   userId); //从数据库中获取数据
            refreshCache(examQueryDTO.getType(),   userId);
        }
        return examVOList;
    }


    //刷新缓存逻辑
    public void refreshCache(Integer examListType, Long userId) {
        List<Exam> examList = new ArrayList<>();
        if (ExamListType.EXAM_UN_FINISH_LIST.getValue().equals(examListType)) {
            //查询未完成竞赛
            examList = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                    .select(Exam::getExamId, Exam::getTitle, Exam::getStartTime, Exam::getEndTime)
                    .gt(Exam::getEndTime, LocalDateTime.now())
                    .eq(Exam::getStatus, Constants.TRUE)
                    .orderByDesc(Exam::getCreateTime));

        } else if (ExamListType.EXAM_HISTORY_LIST.getValue().equals(examListType)) {
            //查询历史竞赛
            examList = examMapper.selectList(new LambdaQueryWrapper<Exam>()
                    .select(Exam::getExamId, Exam::getTitle, Exam::getStartTime, Exam::getEndTime)
                    .le(Exam::getEndTime, LocalDateTime.now())
                    .eq(Exam::getStatus, Constants.TRUE)
                    .orderByDesc(Exam::getCreateTime));
        } else if (ExamListType.USER_EXAM_LIST.getValue().equals( examListType)) {
            //  查询用户竞赛
            List<ExamVO> examVOList = userExamMapper.selectUserExamList(userId);
            BeanUtil.copyToList( examList, Exam.class);
        }
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
        redisService.deleteObject(getExamListKey(examListType, userId));
        redisService.rightPushAll(getExamListKey(examListType, userId), examIdList); //刷新列表缓存
    }


    private List<ExamVO> getExamListByDB(ExamQueryDTO examQueryDTO, Long  userId) {
        PageHelper.startPage(examQueryDTO.getPageNum(), examQueryDTO.getPageSize());
        if (ExamListType.USER_EXAM_LIST.getValue().equals( examQueryDTO.getType())){
            // 查询我的竞赛列表
            return userExamMapper.selectUserExamList(userId);
        } else {
            // 查询C端的竞赛列表
            return examMapper.selectExamList(examQueryDTO);
        }
    }


    private List<ExamVO> assembleExamVOList(List<Long> examIdList) {
        if (CollectionUtil.isEmpty(examIdList)) {
            //说明redis当中没数据 从数据库中查数据并且重新刷新缓存
            return null;
        }
        //拼接redis中key，并将拼接好的key 添加到detailKeyList中
        List<String> detailKeyList = new ArrayList<>();
        for (Long examId : examIdList) {
            detailKeyList.add(getDetailKey(examId));
        }
        List<ExamVO> examVOList = redisService.multiGet(detailKeyList, ExamVO.class);
        CollUtil.removeNull(examVOList);
        if (CollectionUtil.isEmpty(examVOList) || examVOList.size() != examIdList.size()) {
            //说明redis中数据有问题 从数据库中查数据并且重新刷新缓存
            return null;
        }
        return examVOList;
    }


    private String getExamListKey(Integer examListType, Long  userId) {
        if (ExamListType.EXAM_UN_FINISH_LIST.getValue().equals(examListType)) { //  未完成列表
            return CacheConstants.EXAM_UNFINISHED_LIST;
        } else if (ExamListType.EXAM_HISTORY_LIST.getValue().equals(examListType)) { // 历史列表
            return CacheConstants.EXAM_HISTORY_LIST;
        } else { // 用户竞赛列表
             return CacheConstants.USER_EXAM_LIST + userId;
        }
    }


    private String getDetailKey(Long examId) {
        return CacheConstants.EXAM_DETAIL + examId;
    }

    private String getUserExamListKey(Long userId) {
        return CacheConstants.USER_EXAM_LIST + userId;
    }

}