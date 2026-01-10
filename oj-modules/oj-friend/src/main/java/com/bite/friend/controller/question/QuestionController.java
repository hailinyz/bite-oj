package com.bite.friend.controller.question;

import com.bite.common.core.controller.BaseController;
import com.bite.common.core.domain.R;
import com.bite.common.core.domain.TableDataInfo;
import com.bite.friend.domain.question.dto.QuestionQueryDTO;
import com.bite.friend.domain.question.vo.QuestionDetailVO;
import com.bite.friend.service.question.IQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/question")
public class QuestionController extends BaseController {

    @Autowired
    private IQuestionService questionService;


    /*
    查询题目列表
     */
    @GetMapping("/semiLogin/list")
    public TableDataInfo list(QuestionQueryDTO questionQueryDTO) {
        return questionService.list(questionQueryDTO);
    }

    /*
    获取题目详情
     */
    @GetMapping("detail")
    public R<QuestionDetailVO> detailVOR(Long questionId){
        //如果ES查不到，再去数据库查
        return R.ok(questionService.detail(questionId));
    }

}
