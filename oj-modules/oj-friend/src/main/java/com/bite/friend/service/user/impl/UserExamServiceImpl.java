package com.bite.friend.service.user.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bite.common.core.constants.Constants;
import com.bite.common.core.domain.TableDataInfo;
import com.bite.common.core.enums.ExamListType;
import com.bite.common.core.enums.ResultCode;
import com.bite.common.core.util.ThreadLocalUtil;
import com.bite.common.security.exception.ServiceException;
import com.bite.common.security.service.TokenService;
import com.bite.friend.domain.exam.Exam;
import com.bite.friend.domain.exam.dto.ExamQueryDTO;
import com.bite.friend.domain.exam.vo.ExamVO;
import com.bite.friend.domain.user.UserExam;
import com.bite.friend.manger.ExamCacheManager;
import com.bite.friend.mapper.exam.ExamMapper;
import com.bite.friend.mapper.user.UserExamMapper;
import com.bite.friend.service.user.IUserExamService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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

    /*
     * 我的竞赛
     */
    @Override
    public TableDataInfo list(ExamQueryDTO examQueryDTO) {
        //从ThreadLocal中获取用户id
        Long userId = ThreadLocalUtil.get(Constants.USER_ID, Long.class);
        examQueryDTO.setType(ExamListType.USER_EXAM_LIST.getValue()); //设置查询类型为我的竞赛
        //从redis中获取 竞赛列表数据
        Long total = examCacheManager.getListSize(ExamListType.USER_EXAM_LIST.getValue(), userId);
        List<ExamVO> examVOList;
        if (total == null || total <= 0){
            //从数据库中获取 我的竞赛列表数据
            PageHelper.startPage(examQueryDTO.getPageNum(),examQueryDTO.getPageSize());
            examVOList = userExamMapper.selectUserExamList(userId);
            //同步到redis中
            examCacheManager.refreshCache(ExamListType.USER_EXAM_LIST.getValue(), userId);
            total = new PageInfo<>(examVOList).getTotal(); //获取总记录数
        } else {
            //从redis中获取 竞赛列表数据
            examVOList = examCacheManager.getExamVOList(examQueryDTO, userId);
            total =  examCacheManager.getListSize(examQueryDTO.getType(), userId); // 获取总记录数
        }
        if (CollectionUtil.isEmpty(examVOList)){ //使用hutool工具包判断集合是否为空
            return TableDataInfo.empty(); //未查出任何数据时调用
        }
        return TableDataInfo.success(examVOList, total);
    }


}
