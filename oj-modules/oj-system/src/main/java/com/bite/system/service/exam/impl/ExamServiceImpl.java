package com.bite.system.service.exam.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bite.common.core.enums.ResultCode;
import com.bite.common.security.exception.ServiceException;
import com.bite.system.domain.exam.Exam;
import com.bite.system.domain.exam.ExamQuestion;
import com.bite.system.domain.exam.dto.ExamAddDTO;
import com.bite.system.domain.exam.dto.ExamQueryDTO;
import com.bite.system.domain.exam.dto.ExamQuestionAddDTO;
import com.bite.system.domain.exam.vo.ExamVO;
import com.bite.system.domain.question.Question;
import com.bite.system.mapper.exam.examMapper;
import com.bite.system.mapper.exam.examQuestionMapper;
import com.bite.system.mapper.question.QuestionMapper;
import com.bite.system.service.exam.ExamService;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ExamServiceImpl extends ServiceImpl<examQuestionMapper, ExamQuestion> implements ExamService {

    @Autowired
    private examMapper examMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private examQuestionMapper examQuestionMapper;


    /*
    * 查询竞赛列表
     */
    @Override
    public List<ExamVO> list(ExamQueryDTO examQueryDTO) {
        PageHelper.startPage(examQueryDTO.getPageNum(),examQueryDTO.getPageSize());
        return examMapper.selectExamList(examQueryDTO);
    }

    /*
    * 添加竞赛 - 不包含题目的新增
     */
    @Override
    public int add(ExamAddDTO examAddDTO) {

        //竞赛名称的重复性校验
        List<Exam> examList = examMapper
                .selectList(new LambdaQueryWrapper<Exam>()
                        .eq(Exam::getTitle, examAddDTO.getTitle()));
        if (CollectionUtil.isNotEmpty(examList)) {
            throw new ServiceException(ResultCode.FAILED_ALREADY_EXISTS);
        }
        //进行时间的校验
        if (examAddDTO.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException(ResultCode.EXAM_START_TIME_BEFORE_CURRENT_TIME);
        }
        if (examAddDTO.getStartTime().isAfter(examAddDTO.getEndTime())) {
            throw new ServiceException(ResultCode.EXAM_START_TIME_AFTER_END_TIME);
        }

        //处理传过来的参数，将DTO转为实体
        Exam exam = new Exam();
        BeanUtil.copyProperties(examAddDTO,exam);
        return examMapper.insert(exam);
    }

    /*
    * 添加竞赛 - 包含题目的新增
     */
    @Override
    public boolean addQuestion(ExamQuestionAddDTO examQuestionAddDTO) {
        //对参数进行校验：对竞赛id判断 和 题目id集合判断
        Exam exam = getExam(examQuestionAddDTO); //第一处优化
        Set<Long> questionIdSet = examQuestionAddDTO.getQuestionIdSet();
        if (CollectionUtil.isEmpty(questionIdSet)){
            return true;
        }

        //批量查询
        List<Question> questionList = questionMapper.selectBatchIds(questionIdSet);
        if (CollectionUtil.isEmpty(questionList) || questionList.size() != questionIdSet.size()){
            throw new ServiceException(ResultCode.EXAM_QUESTION_NOT_EXISTS);
        }

        //批量保存
        return saveExamQuestion(examQuestionAddDTO, questionIdSet);

    }

    /*
    * 批量保存
     */
    private boolean saveExamQuestion(ExamQuestionAddDTO examQuestionAddDTO, Set<Long> questionIdSet) {
        int number = 1;
        List<ExamQuestion> examQuestionList = new ArrayList<>();
        for (Long questionId : questionIdSet){
            //判断question是否为空
/*            Question question = questionMapper.selectById(questionId);
            if (question == null){
                throw new ServiceException(ResultCode.EXAM_QUESTION_NOT_EXISTS);
            }*/
            //到这里就可以进行添加了
            ExamQuestion examQuestion = new ExamQuestion();
            examQuestion.setExamId(examQuestionAddDTO.getExamId());
            examQuestion.setQuestionId(questionId);
            examQuestion.setQuestionOrder(number++);
            examQuestionList.add(examQuestion);
//            examQuestionMapper.insert(examQuestion);
        }
        return saveBatch(examQuestionList);
    }

    private Exam getExam(ExamQuestionAddDTO examQuestionAddDTO) {
        //对参数进行校验：对竞赛id判断 和 题目id集合判断
        Exam exam = examMapper.selectById(examQuestionAddDTO.getExamId());
        if (exam == null) {
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }
        return exam;
    }


}
