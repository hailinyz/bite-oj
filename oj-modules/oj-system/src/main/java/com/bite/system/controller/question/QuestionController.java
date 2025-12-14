package com.bite.system.controller.question;

import com.bite.common.core.controller.BaseController;
import com.bite.common.core.domain.R;
import com.bite.common.core.domain.TableDataInfo;
import com.bite.system.domain.question.dto.QuestionAddDTO;
import com.bite.system.domain.question.dto.QuestionEditDTO;
import com.bite.system.domain.question.dto.QuestionQueryDTO;
import com.bite.system.domain.question.vo.QuestionDetailVO;
import com.bite.system.service.question.IQuestionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/question")
@Tag(name = "题目管理接口")
public class QuestionController extends BaseController {

    @Autowired
    private IQuestionService questionService;

    /*
    * 获取题目列表接口
     */
    @GetMapping("/list")
    public TableDataInfo list(QuestionQueryDTO questionQueryDTO){
        return getTableDataInfo(questionService.list(questionQueryDTO));
    }

    /*
    * 添加题目接口
     */
    @PostMapping("/add")
    public R<Void> add(@RequestBody QuestionAddDTO questionAddDTO){
        return toR(questionService.add(questionAddDTO));
    }

    /*
    * 查询题目详情
     */
    @GetMapping("/detail")
    public R<QuestionDetailVO> detail(Long questionId){
        return R.ok(questionService.detail(questionId));
    }

    /*
    * 编辑题目接口
     */
    @PutMapping("/edit")
    public R<Void> edit(@RequestBody QuestionEditDTO questionEditDTO){
        return toR(questionService.edit(questionEditDTO));
    }

    /*
    * 删除题目接口
     */
    @DeleteMapping("/delete")
    public R<Void> delete(Long questionId){
        return toR(questionService.delete(questionId));
    }

}
