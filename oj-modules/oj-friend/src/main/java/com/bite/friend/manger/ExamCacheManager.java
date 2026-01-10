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
import com.bite.friend.domain.exam.ExamQuestion;
import com.bite.friend.domain.exam.dto.ExamQueryDTO;
import com.bite.friend.domain.exam.vo.ExamVO;
import com.bite.friend.domain.user.UserExam;
import com.bite.friend.mapper.exam.ExamMapper;
import com.bite.friend.mapper.exam.examQuestionMapper;
import com.bite.friend.mapper.user.UserExamMapper;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class ExamCacheManager {

    @Autowired
    private RedisService redisService;

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private UserExamMapper userExamMapper;

    @Autowired
    private examQuestionMapper examQuestionMapper;

    /*
    将用户竞赛信息存储到 redis中
     */
    public void addUserExamCache(Long  userId, Long  examId){
        String userExamListKey = getUserExamListKey(userId);
        redisService.leftPushForList( userExamListKey, examId);
    }


    /*
    获取列表数据大小,查看redis中是否存在数据
     */
    public Long getListSize(Integer examListType, Long  userId) {
        String examListKey = getExamListKey(examListType,  userId);
        return redisService.getListSize(examListKey);
    }

    /*
    获取竞赛中题目列表数据
     */
    public Long getExamQuestionListSize(Long examId){
        String examQuestionListKey = getExamQuestionListKey(examId);
        return redisService.getListSize(examQuestionListKey);
    }


    /*
     * 获取第一个题目
     */
    public Long getFirstQuestion(Long examId) {
        return redisService.indexForList(getExamQuestionListKey(examId), 0, Long.class);
    }


    public List<ExamVO> getExamVOList(ExamQueryDTO examQueryDTO,  Long userId) {
        int start = (examQueryDTO.getPageNum() - 1) * examQueryDTO.getPageSize();
        int end = start + examQueryDTO.getPageSize() - 1; //下标需要-1
        String examListKey = getExamListKey(examQueryDTO.getType(),   userId);
        List<Long> examIdList = redisService.getCacheListByRange(examListKey, start, end, Long.class);
        List<ExamVO> examVOList = assembleExamVOList(examIdList);
        if (CollectionUtil.isEmpty(examVOList)) {
            //说明redis中数据可能有问题 从数据库中查数据并且重新刷新缓存
            examVOList = getExamListByDB(examQueryDTO, userId); //从数据库中获取数据
            refreshCache(examQueryDTO.getType(),   userId);
        }
        return examVOList;
    }

    /*
     获取用户所有竞赛
     */
    public List<Long> getAllUserExamList(Long userId) {
        //首先拿到redis中的key (把当前用户所有报名过竞赛redis当中的key)
         String examListKey = CacheConstants.USER_EXAM_LIST +  userId;
         // 拿到redis中所有的 值，不需要分 割，直接获取所有数据
        List<Long> userExamIdList = redisService.getCacheListByRange(examListKey, 0, -1, Long.class);
        if ( CollectionUtil.isNotEmpty(userExamIdList)){
            return userExamIdList;
        } else  {
            //说明redis当中没数据 从数据库中查数据并且重新刷新缓存
            List<UserExam> userExamList = userExamMapper.selectList(new LambdaQueryWrapper<UserExam>()
                    .eq(UserExam::getUserId, userId));
            if ( CollectionUtil.isEmpty(userExamList)){
                 return null;
            }
            refreshCache(ExamListType.USER_EXAM_LIST.getValue(),userId);
            //最后拿到的是竞赛的 id列表
            return  userExamList.stream().map(UserExam::getExamId).collect(Collectors.toList());
        }
    }


    /*
     * 刷新竞赛题目缓存
     */
    public void refreshExamQuestionCache(Long examId) {

        //查询竞赛的所有题目
        List<ExamQuestion> examQuestionList = examQuestionMapper.selectList(new LambdaQueryWrapper<ExamQuestion>()
                .select(ExamQuestion::getQuestionId)
                .eq(ExamQuestion::getExamId, examId)
                .orderByAsc(ExamQuestion::getQuestionOrder));
        if (CollectionUtil.isEmpty(examQuestionList)){
            return;
        }
        //提取题目ID列表
        List<Long> examQuestionIdList = examQuestionList.stream().map(ExamQuestion::getQuestionId).toList();

        //将题目ID列表存入Redis缓存
        redisService.rightPushAll(getExamQuestionListKey(examId), examQuestionIdList);

        //节省redis缓存资源 1天
        long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(),
               LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0));
        redisService.expire(getExamQuestionListKey(examId), seconds, TimeUnit.SECONDS);
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

    /*
     * 获取竞赛题目列表缓存的key
     */
    private String getExamQuestionListKey(Long examId) {
        return CacheConstants.EXAM_QUESTION_LIST + examId;
    }






}