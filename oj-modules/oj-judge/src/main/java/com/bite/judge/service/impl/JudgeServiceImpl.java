package com.bite.judge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bite.api.domain.UserExeResult;
import com.bite.api.domain.dto.JudgeSubmitDTO;
import com.bite.api.domain.vo.UserQuestionResultVO;
import com.bite.common.core.constants.Constants;
import com.bite.common.core.constants.JudgeConstants;
import com.bite.common.core.enums.CodeRunStatus;
import com.bite.judge.domain.SandBoxExecuteResult;
import com.bite.judge.domain.UserSubmit;
import com.bite.judge.mapper.UserSubmitMapper;
import com.bite.judge.service.IJudgeService;
import com.bite.judge.service.ISandboxService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JudgeServiceImpl implements IJudgeService {

    @Autowired
    private ISandboxService sandboxService;

    @Autowired
    private UserSubmitMapper userSubmitMapper;

    /*
    判题接口
     */
    @Override
    public UserQuestionResultVO doJudgeJavaCode(JudgeSubmitDTO judgeSubmitDTO) {
        SandBoxExecuteResult sandBoxExecuteResult =
                sandboxService.exeJavaCode(judgeSubmitDTO.getUserCode(), judgeSubmitDTO.getInputList()); //拿到在docker的执行结果
        UserQuestionResultVO userQuestionResultVO = new UserQuestionResultVO();

        if (sandBoxExecuteResult != null && sandBoxExecuteResult.getRunStatus().equals(CodeRunStatus.SUCCEED)){
            //对比执行结果 时间限制、空间限制的比对
            userQuestionResultVO = doJudge(judgeSubmitDTO, sandBoxExecuteResult, userQuestionResultVO);

        } else {
            userQuestionResultVO.setPass(Constants.FALSE); //未通过
            if (sandBoxExecuteResult != null){
                userQuestionResultVO.setExeMessage(sandBoxExecuteResult.getExeMessage());
            }else {
                userQuestionResultVO.setExeMessage(CodeRunStatus.UNKNOWN_FAILED.getMsg());
            }
            userQuestionResultVO.setScore(JudgeConstants.ERROR_SCORE); //0分
        }

        return saveUserSubmit(judgeSubmitDTO, userQuestionResultVO);
    }

    private static UserQuestionResultVO doJudge(JudgeSubmitDTO judgeSubmitDTO,
                                SandBoxExecuteResult sandBoxExecuteResult,
                                UserQuestionResultVO userQuestionResultVO) {
        //比对执行结果  时间限制、空间限制的比对
        List<String> exeOutoutList = sandBoxExecuteResult.getOutputList();
        List<String> outputList = judgeSubmitDTO.getOutputList();
        if (outputList.size() != exeOutoutList.size()){  //长度不一致(数量上是否相等的判断)
            userQuestionResultVO.setScore(JudgeConstants.ERROR_SCORE);
            userQuestionResultVO.setPass(Constants.FALSE);
            userQuestionResultVO.setExeMessage(CodeRunStatus.NOT_ALL_PASSED.getMsg());
            return userQuestionResultVO;
        }  //长度一致，然后对输出结果进行逐个比对
            List<UserExeResult> userExeResultList = new ArrayList<>();
        //结果比对
        boolean passed = resultCompare(judgeSubmitDTO, outputList, exeOutoutList);

        //组装结果（判断时间、空间限制）
        return assembleUserQuestionResultVO(judgeSubmitDTO, sandBoxExecuteResult, userQuestionResultVO, userExeResultList, passed);
    }

    /*
    通过一些判断将结果组装成UserQuestionResultVO
     */
    private static UserQuestionResultVO assembleUserQuestionResultVO(JudgeSubmitDTO judgeSubmitDTO,
                                                                     SandBoxExecuteResult sandBoxExecuteResult,
                                                                     UserQuestionResultVO userQuestionResultVO,
                                                                     List<UserExeResult> userExeResultList,
                                                                     boolean passed) {
        userQuestionResultVO.setUserExeResultList(userExeResultList);
        if (!passed){
            userQuestionResultVO.setScore(JudgeConstants.ERROR_SCORE);
            userQuestionResultVO.setPass(Constants.FALSE);
            userQuestionResultVO.setExeMessage(CodeRunStatus.NOT_ALL_PASSED.getMsg());
            return userQuestionResultVO;
        }
        //输出结果一致，比对时间限制&空间限制
        if (sandBoxExecuteResult.getUseMemory() > judgeSubmitDTO.getSpaceLimit()){
            userQuestionResultVO.setScore(JudgeConstants.ERROR_SCORE);
            userQuestionResultVO.setPass(Constants.FALSE);
            userQuestionResultVO.setExeMessage(CodeRunStatus.OUT_OF_MEMORY.getMsg());
            return userQuestionResultVO;
        }
        if (sandBoxExecuteResult.getUseTime() > judgeSubmitDTO.getTimeLimit()){
            userQuestionResultVO.setScore(JudgeConstants.ERROR_SCORE);
            userQuestionResultVO.setPass(Constants.FALSE);
            userQuestionResultVO.setExeMessage(CodeRunStatus.OUT_OF_TIME.getMsg());
            return userQuestionResultVO;
        }
        //输出结果一致，时间限制&空间限制一致 --> 正确
        userQuestionResultVO.setPass(Constants.TRUE);
        int score = judgeSubmitDTO.getDifficulty() * JudgeConstants.DEFAULT_SCORE;
        userQuestionResultVO.setScore(score);
        return userQuestionResultVO;
    }

    /*
    结果比对
     */
    private static boolean resultCompare(JudgeSubmitDTO judgeSubmitDTO, List<String> outputList, List<String> exeOutoutList) {
        boolean passed = true;
        for (int index = 0; index < outputList.size(); index++) {
            String output = outputList.get(index);
            String exeOutput = exeOutoutList.get(index);
            String input = judgeSubmitDTO.getInputList().get(index);
            UserExeResult userExeResult = new UserExeResult();
            userExeResult.setInput(input);
            userExeResult.setOutput(output);
            userExeResult.setExeOutput(exeOutput);
            if (!output.equals(exeOutput)){
                passed = false;
            }
        }
        return passed;
    }


    private UserQuestionResultVO saveUserSubmit(JudgeSubmitDTO judgeSubmitDTO, UserQuestionResultVO userQuestionResultVO) {
        UserSubmit userSubmit = new UserSubmit(); //需要存入的实体 存入数据库
        BeanUtils.copyProperties(judgeSubmitDTO, userSubmit);
        userSubmit.setUserId(judgeSubmitDTO.getUserId());
        userSubmit.setQuestionId(judgeSubmitDTO.getQuestionId());
        userSubmit.setExamId(judgeSubmitDTO.getExamId());
        userSubmit.setProgramType(judgeSubmitDTO.getProgramType());
        userSubmit.setUserCode(judgeSubmitDTO.getUserCode());
//        userSubmitMapper.insert(userSubmit); //我们现在以最后一次提交为准，但是这个是每次提交都保存,所以我们先删除再插入
        userSubmitMapper.delete(new LambdaQueryWrapper<UserSubmit>()
                .eq(UserSubmit::getUserId, judgeSubmitDTO.getUserId())
                .eq(UserSubmit::getQuestionId, judgeSubmitDTO.getQuestionId())
                .isNull(judgeSubmitDTO.getExamId() == null, UserSubmit::getExamId)
                .eq(judgeSubmitDTO.getExamId() != null, UserSubmit::getExamId, judgeSubmitDTO.getExamId()));
        userSubmitMapper.insert(userSubmit); //先删除再插入
        return userQuestionResultVO;
    }


}
