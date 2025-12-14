package com.bite.system.controller.exam;

import com.bite.common.core.controller.BaseController;
import com.bite.common.core.domain.TableDataInfo;
import com.bite.system.domain.exam.dto.ExamQueryDTO;
import com.bite.system.service.exam.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
