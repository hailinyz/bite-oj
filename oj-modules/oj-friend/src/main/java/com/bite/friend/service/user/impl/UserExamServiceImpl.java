package com.bite.friend.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bite.common.core.constants.Constants;
import com.bite.common.core.enums.ResultCode;
import com.bite.common.core.util.ThreadLocalUtil;
import com.bite.common.security.exception.ServiceException;
import com.bite.common.security.service.TokenService;
import com.bite.friend.domain.exam.Exam;
import com.bite.friend.domain.user.UserExam;
import com.bite.friend.manger.ExamCacheManager;
import com.bite.friend.mapper.exam.ExamMapper;
import com.bite.friend.mapper.user.UserExamMapper;
import com.bite.friend.service.user.IUserExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserExamServiceImpl implements IUserExamService {

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private UserExamMapper userExamMapper;

    @Autowired
    private TokenService tokenService;

    @Value("${jwt.secret}")
    private String secret;

    @Autowired
    private ExamCacheManager examCacheManager;


    /*
     * 竞赛报名
     */
    @Override
    public int enter(String token, Long examId) {

        //判断竞赛是否存在 (解决不能报名不存在竞赛问题)
        Exam exam = examMapper.selectById(examId);
        if (exam == null){
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }

        //判断竞赛是否开赛
        if (exam.getStartTime().isBefore(LocalDateTime.now())){
            throw new ServiceException(ResultCode.EXAM_STARTED);
        }

        //不能重复报名的判断
        //Long userId = tokenService.getUserId(token, secret); //获取用户id（其实已经在网关就能获取到userId了）
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        UserExam userExam = userExamMapper.selectOne(new LambdaQueryWrapper<UserExam>()
                .eq(UserExam::getExamId, examId)
                .eq(UserExam::getUserId, userId));
        if (userExam != null){
            throw new ServiceException(ResultCode.USER_EXAM_HAS_ENTER);
        }

        //将用户竞赛信息存储到 redis中
        examCacheManager.addUserExamCache(userId, examId);
        //将用户竞赛信息存储到数据库中
        userExam = new UserExam();
        userExam.setExamId(examId);
        userExam.setUserId(userId);
        return userExamMapper.insert(userExam);
    }





}
