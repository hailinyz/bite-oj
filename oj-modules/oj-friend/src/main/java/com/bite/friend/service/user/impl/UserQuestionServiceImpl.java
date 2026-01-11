package com.bite.friend.service.user.impl;

import cn.hutool.json.JSONUtil;
import com.bite.api.RemoteJudgeService;
import com.bite.api.domain.dto.JudgeSubmitDTO;
import com.bite.api.domain.vo.UserQuestionResultVO;
import com.bite.common.core.constants.Constants;
import com.bite.common.core.domain.R;
import com.bite.common.core.enums.ProgramType;
import com.bite.common.core.enums.ResultCode;
import com.bite.common.core.util.ThreadLocalUtil;
import com.bite.common.security.exception.ServiceException;
import com.bite.friend.domain.question.Question;
import com.bite.friend.domain.question.QuestionCase;
import com.bite.friend.domain.user.dto.UserSubmitDTO;
import com.bite.friend.mapper.question.QuestionMapper;
import com.bite.friend.service.user.IUserQuestionService;
import com.bite.system.domain.question.es.QuestionES;
import com.bite.system.elasticsearch.QuestionRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserQuestionServiceImpl implements IUserQuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private RemoteJudgeService remoteJudgeService;

    /*
    接收用户代码提交 --> 判题 --> 返回结果
     */
    @Override
    public R<UserQuestionResultVO> submit(UserSubmitDTO userSubmitDTO) {
        Integer programType = userSubmitDTO.getProgramType();
        //判断程序语言类型
        if (ProgramType.JAVA.getValue().equals(programType)){
            //按照java语言逻辑处理
            JudgeSubmitDTO judgeSubmitDTO = assembleJudgeSubmitDTO(userSubmitDTO);
            //调用判题服务 -- 通过openFeign调用
            return remoteJudgeService.doJudgeJavaCode(judgeSubmitDTO);
        }
        throw new ServiceException(ResultCode.FAILED_NOT_SUPPORT_PROGRAM);
    }

    /*
    组装判题参数所需要的数据返回给判题服务（judge）  -- 准备判题所需的所有数据
     */
    private JudgeSubmitDTO assembleJudgeSubmitDTO(UserSubmitDTO userSubmitDTO) {
        //从ES中查询题目信息
        Long questionId = userSubmitDTO.getQuestionId();
        QuestionES questionES = questionRepository.findById(questionId).orElse(null);
        JudgeSubmitDTO judgeSubmitDTO = new JudgeSubmitDTO();
        if (questionES != null){
            BeanUtils.copyProperties(questionES,judgeSubmitDTO);
        } else {
            Question question = questionMapper.selectById(questionId);
            BeanUtils.copyProperties(question,judgeSubmitDTO);

            //保存到ES中（同步）
            questionES = new QuestionES();
            BeanUtils.copyProperties(question,questionES);
            questionRepository.save(questionES);
        }
        //设置用户和竞赛信息
        judgeSubmitDTO.setUserId(ThreadLocalUtil.get(Constants.USER_ID, Long.class));
        judgeSubmitDTO.setExamId(userSubmitDTO.getExamId());
        judgeSubmitDTO.setProgramType(userSubmitDTO.getProgramType());
        //拼接完整代码
        judgeSubmitDTO.setUserCode(codeConnect(userSubmitDTO.getUserCode(), questionES.getMainFuc()));
        //解析测试用例
        List<QuestionCase> questionCaseList = JSONUtil.toList(questionES.getQuestionCase(), QuestionCase.class);// 将json字符串转换成List
        //用流的方式分成两个集合
        List<String> inputList = questionCaseList.stream().map(QuestionCase::getInput).toList();
        judgeSubmitDTO.setInputList(inputList);
        List<String> outputList = questionCaseList.stream().map(QuestionCase::getOutput).toList();
        judgeSubmitDTO.setOutputList(outputList);

        return judgeSubmitDTO;

    }

    //用户代码的拼接
    private String codeConnect(String userCode,String mainFunc){
        String targetCharacter = "}";
        int targetLastIndex = userCode.lastIndexOf(targetCharacter);
        System.out.println(targetLastIndex);
        if (targetLastIndex != -1){
            return userCode.substring(0,targetLastIndex) + "\n" + mainFunc + "\n" + userCode.substring(targetLastIndex);
        }
        throw new ServiceException(ResultCode.FAILED);
    }



}
