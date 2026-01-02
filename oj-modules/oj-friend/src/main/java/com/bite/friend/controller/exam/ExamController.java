package com.bite.friend.controller.exam;


import com.bite.common.core.controller.BaseController;
import com.bite.common.core.domain.TableDataInfo;
import com.bite.friend.domain.exam.dto.ExamQueryDTO;
import com.bite.friend.service.exam.IExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/exam")
public class ExamController extends BaseController {


    @Autowired
    private IExamService examService;


    /*
     * 查询竞赛列表（老接口--> 未加入redis优化）
     */
    @GetMapping("/semiLogin/list")
    public TableDataInfo list(ExamQueryDTO examQueryDTO){
        return getTableDataInfo(examService.list(examQueryDTO));
    }

    /*
     * 查询竞赛列表（新接口--> 加入了redis优化）
     */
    @GetMapping("/semiLogin/redis/list")
    public TableDataInfo redisList(ExamQueryDTO examQueryDTO){
        examService.redisList(examQueryDTO);
    }

}
