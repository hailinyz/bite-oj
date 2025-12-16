package com.bite.system.service.exam.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bite.common.core.constants.Constants;
import com.bite.common.core.enums.ResultCode;
import com.bite.common.security.exception.ServiceException;
import com.bite.system.domain.exam.Exam;
import com.bite.system.domain.exam.ExamQuestion;
import com.bite.system.domain.exam.dto.ExamAddDTO;
import com.bite.system.domain.exam.dto.ExamEditDTO;
import com.bite.system.domain.exam.dto.ExamQueryDTO;
import com.bite.system.domain.exam.dto.ExamQuestionAddDTO;
import com.bite.system.domain.exam.vo.ExamDtailVO;
import com.bite.system.domain.exam.vo.ExamVO;
import com.bite.system.domain.question.Question;
import com.bite.system.domain.question.vo.QuestionVO;
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
    public String add(ExamAddDTO examAddDTO) {

        checkExamSaveParams(examAddDTO, null);

        //处理传过来的参数，将DTO转为实体
        Exam exam = new Exam();
        BeanUtil.copyProperties(examAddDTO,exam);
        examMapper.insert(exam);
        return exam.getExamId().toString();
    }

    /*
    * 添加竞赛 - 包含题目的新增
     */
    @Override
    public boolean addQuestion(ExamQuestionAddDTO examQuestionAddDTO) {
        //对参数进行校验：对竞赛id判断 和 题目id集合判断
        Exam exam = getExam(examQuestionAddDTO.getExamId()); //第一处优化
        checkExam(exam);
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
    * 根据id查询竞赛详情
     */
    @Override
    public ExamDtailVO detail(Long examId) {
        ExamDtailVO examDtailVO = new ExamDtailVO();
        //获取竞赛基本信息
        Exam exam = getExam(examId);
        BeanUtil.copyProperties(exam,examDtailVO);
        //获取题目id集合
        List<ExamQuestion> examQuestionList = examQuestionMapper.selectList(new LambdaQueryWrapper<ExamQuestion>()
                .select(ExamQuestion::getQuestionId)
                .eq(ExamQuestion::getExamId, examId)
                .orderByAsc(ExamQuestion::getQuestionOrder));
        if (CollectionUtil.isEmpty(examQuestionList)){ //有可能是没有题目的竞赛
            //只包含竞赛基本信息
            return examDtailVO;
        }
        //先拿到题目id的集合到新的List 集合中
        List<Long> questionIdList = examQuestionList.stream().map(ExamQuestion::getQuestionId).toList();
        //批量在题目表中根据id集合查询
        List<Question> questionList = questionMapper.selectList(new LambdaQueryWrapper<Question>()
                .select(Question::getQuestionId, Question::getTitle, Question::getDifficulty)
                .in(Question::getQuestionId, questionIdList));
        //将题目信息转为VO
//        List<QuestionVO> quesiontVOList = new ArrayList<>();
        List<QuestionVO> quesiontVOList = BeanUtil.copyToList(questionList,QuestionVO.class);
        examDtailVO.setExamQuestionList(quesiontVOList);
        return examDtailVO;
    }

    /*
    编辑竞赛基本信息
     */
    @Override
    public int edit(ExamEditDTO examEditDTO) {
        Exam exam = getExam(examEditDTO.getExamId());
        checkExam(exam);
        checkExamSaveParams(examEditDTO, examEditDTO.getExamId());
        //更新并且对时间、标题等进行校验
        exam.setTitle(examEditDTO.getTitle());
        exam.setStartTime(examEditDTO.getStartTime());
        exam.setEndTime(examEditDTO.getEndTime());
        //将更新的数据保存到数据库中
        return examMapper.updateById(exam);
    }

    /*
    判断竞赛能否被进行操作
     */
    private static void checkExam(Exam exam) {
        if (exam.getStartTime().isBefore(LocalDateTime.now())){
            throw new ServiceException(ResultCode.EXAM_STARTED);
        }
    }

    /*
    删除竞赛中题目
     */
    @Override
    public int questionDelete(Long examId, Long questionId) {
        Exam exam = getExam(examId);
        checkExam(exam);
        int delete = examQuestionMapper.delete(new LambdaQueryWrapper<ExamQuestion>()
                .eq(ExamQuestion::getExamId, examId)
                .eq(ExamQuestion::getQuestionId, questionId));
        return delete;
    }

    /*
    删除竞赛
     */
    @Override
    public int delete(Long examId) {
        //判断竞赛是否存在
        Exam exam = getExam(examId);
        //判断竞赛是否开始
        checkExam(exam);
        //删除竞赛中的题目
        examQuestionMapper.delete(new LambdaQueryWrapper<ExamQuestion>()
                .eq(ExamQuestion::getExamId, examId));
        //删除竞赛
        return examMapper.deleteById(exam);
    }

    /*
    发布竞赛
     */
    @Override
    public int publish(Long examId) {
        //判断竞赛是否存在
        Exam exam = getExam(examId);
        //判断竞赛中是否有题目 select count(*) from tb_exam_question where exam_id = #{examId}
        Long count = examQuestionMapper.selectCount(new LambdaQueryWrapper<ExamQuestion>()
                .eq(ExamQuestion::getExamId, examId));
        if (count == null || count <= 0){
            throw new ServiceException(ResultCode.EXAM_NOT_HAS_QUESTION);
        }
        //改变状态并同步到数据库
        exam.setStatus(Constants.TRUE);
        return examMapper.updateById(exam);
    }

    /*
    撤销发布
     */
    @Override
    public int cancelpublish(Long examId) {
        //判断竞赛是否存在
        Exam exam = getExam(examId);
        //判断竞赛是否开始
        checkExam(exam);
        exam.setStatus(Constants.FALSE);
        return examMapper.updateById(exam);
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

    /*
    * 根据id查询竞赛信息
     */
    private Exam getExam(Long examId) {
        //对参数进行校验：对竞赛id判断 和 题目id集合判断
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new ServiceException(ResultCode.FAILED_NOT_EXISTS);
        }
        return exam;
    }

    /*
    * 添加竞赛参数校验
     */
    private void checkExamSaveParams(ExamAddDTO examsaveDTO,Long examId) {
        //1. 对竞赛标题是否重复进行校验  2.竞赛开始、结束时间进行校验
        //竞赛名称的重复性校验
        List<Exam> examList = examMapper
                .selectList(new LambdaQueryWrapper<Exam>()
                        .eq(Exam::getTitle, examsaveDTO.getTitle())
                        .ne(examId != null,Exam::getExamId, examId));
        if (CollectionUtil.isNotEmpty(examList)) {
            throw new ServiceException(ResultCode.FAILED_ALREADY_EXISTS);
        }
        //进行时间的校验
        if (examsaveDTO.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException(ResultCode.EXAM_START_TIME_BEFORE_CURRENT_TIME);
        }
        if (examsaveDTO.getStartTime().isAfter(examsaveDTO.getEndTime())) {
            throw new ServiceException(ResultCode.EXAM_START_TIME_AFTER_END_TIME);
        }
    }


}
