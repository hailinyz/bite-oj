package com.bite.friend.controller.exam;

import com.bite.common.core.controller.BaseController;
import com.bite.common.core.domain.R;
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
        return examService.redisList(examQueryDTO);
    }

    /*
     *获取竞赛中第一道题目的id接口**
     */
    @GetMapping("getFirstQuestion")
    public R<String> getFirstQuestion(Long examId){

        //代码逻辑：获取竞赛中题目的顺序列表， 先从redis  redis没有再从数据库 key：e:q:l:examId  value：questionId
        // 排在第一个的题目 返回给前端

        return R.ok(examService.getFirstQuestion(examId));

    }

    /*
    获取上一题（竞赛内）
     */
    @GetMapping("preQuestion")
    public R<String> preQuestion(Long examId, Long questionId){
        return R.ok(examService.preQuestion(examId, questionId));
    }

    /*
    获取下一题（竞赛内）
     */
    @GetMapping("nextQuestion")
    public R<String> nextQuestion(Long examId, Long questionId){
        return R.ok(examService.nextQuestion(examId, questionId));
    }




}
