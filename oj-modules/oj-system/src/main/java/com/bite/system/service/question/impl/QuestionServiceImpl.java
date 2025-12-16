package com.bite.system.service.question.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bite.common.core.constants.Constants;
import com.bite.common.core.enums.ResultCode;
import com.bite.common.security.exception.ServiceException;
import com.bite.system.domain.exam.Exam;
import com.bite.system.domain.question.Question;
import com.bite.system.domain.question.dto.QuestionAddDTO;
import com.bite.system.domain.question.dto.QuestionEditDTO;
import com.bite.system.domain.question.dto.QuestionQueryDTO;
import com.bite.system.domain.question.vo.QuestionDetailVO;
import com.bite.system.domain.question.vo.QuestionVO;
import com.bite.system.mapper.question.QuestionMapper;
import com.bite.system.service.question.IQuestionService;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl implements IQuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    /*
    获取题目列表接口
     */
    @Override
    public List<QuestionVO> list(QuestionQueryDTO questionQueryDTO) {
        String excludeIdStr = questionQueryDTO.getExcludeIdStr();
        if (StrUtil.isNotEmpty(excludeIdStr)){
            String[] excludeIdArr = excludeIdStr.split(Constants.SPLIT_SEM);
            Set<Long> excludeIdSet = Arrays.stream(excludeIdArr)
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());
            questionQueryDTO.setExcludeIdSet(excludeIdSet);
        }
        PageHelper.startPage(questionQueryDTO.getPageNum(),questionQueryDTO.getPageSize());
        return questionMapper.selectQuestionList(questionQueryDTO);
    }

    /*
    添加题目接口
     */
    @Override
    public int add(QuestionAddDTO questionAddDTO) {
        //在添加题目之前，先判断题目标题是否已经存在，只要查看数据库中存在相同标题的题目，则返回错误
        List<Question> questionList = questionMapper.selectList(new LambdaQueryWrapper<Question>()
                .eq(Question::getTitle, questionAddDTO.getTitle()));
        if (CollectionUtil.isNotEmpty(questionList)){
            throw new ServiceException(ResultCode.FAILED_ALREADY_EXISTS);
        }

        //转换DTO对象成实体对象,使用对象的属性拷贝
        Question question = new Question();
        BeanUtils.copyProperties(questionAddDTO,question);
        //插入数据库
        return questionMapper.insert(question);
    }

    /*
    查询题目详情
     */
    @Override
    public QuestionDetailVO detail(Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null){
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }
        //返回题目详情给前端，将实体对象转换成VO对象
        QuestionDetailVO questionDetailVO = new QuestionDetailVO();
        BeanUtils.copyProperties(question,questionDetailVO);

        return questionDetailVO;
    }

    /*
    编辑题目接口
     */
    @Override
    public int edit(QuestionEditDTO questionEditDTO) {
        Question oldquestion = questionMapper.selectById(questionEditDTO.getQuestionId());
        if (oldquestion == null){
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }

        //使用对象属性拷贝，将DTO对象属性拷贝到实体对象中，然后更新数据库
        oldquestion.setTitle(questionEditDTO.getTitle());
        oldquestion.setDifficulty(questionEditDTO.getDifficulty());
        oldquestion.setTimeLimit(questionEditDTO.getTimeLimit());
        oldquestion.setSpaceLimit(questionEditDTO.getSpaceLimit());
        oldquestion.setContent(questionEditDTO.getContent());
        oldquestion.setQuestionCase(questionEditDTO.getQuestionCase());
        oldquestion.setDefaultCode(questionEditDTO.getDefaultCode());
        oldquestion.setMainFunc(questionEditDTO.getMainFunc());
        return questionMapper.updateById(oldquestion);

    }

    /*
    删除题目接口
     */
    @Override
    public int delete(Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null){
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }
        return questionMapper.deleteById(questionId);
    }

    /*
    判断竞赛能否被进行操作
     */
/*    private static void checkExam(Exam exam) {
        if (exam.getStartTime().isBefore(LocalDateTime.now())){
            throw new ServiceException(ResultCode.EXAM_STARTED);
        }
    }*/


}
