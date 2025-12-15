package com.bite.system.controller.exam;

import com.bite.common.core.controller.BaseController;
import com.bite.common.core.domain.R;
import com.bite.common.core.domain.TableDataInfo;
import com.bite.system.domain.exam.Exam;
import com.bite.system.domain.exam.dto.ExamAddDTO;
import com.bite.system.domain.exam.dto.ExamQueryDTO;
import com.bite.system.domain.exam.dto.ExamQuestionAddDTO;
import com.bite.system.domain.exam.vo.ExamDtailVO;
import com.bite.system.service.exam.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/exam")
public class ExamController extends BaseController {

    @Autowired
    private ExamService examService;

    /*
    * 查询竞赛列表
     */
    @GetMapping("/list")
    public TableDataInfo list(ExamQueryDTO examQueryDTO){
        return getTableDataInfo(examService.list(examQueryDTO));
    }

    /*
    * 添加竞赛 - 不包含题目的新增
     */
    @PostMapping("/add")
    public R<String> add(@RequestBody ExamAddDTO examAddDTO){
        return R.ok(examService.add(examAddDTO));
    }

    /*
    * 添加竞赛 - 包含题目的新增
     */
    @PostMapping("/question/add")
    public R<Void> questionaAdd(@RequestBody ExamQuestionAddDTO examQuestionAddDTO){
        return toR(examService.addQuestion(examQuestionAddDTO));
    }

    /*
    * 获取竞赛详情
     */
    @GetMapping("/detail")
    public R<ExamDtailVO> detail(Long examId){
        return R.ok(examService.detail(examId));
    }

}
