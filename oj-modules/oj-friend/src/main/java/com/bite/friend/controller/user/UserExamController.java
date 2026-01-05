package com.bite.friend.controller.user;

import com.bite.common.core.constants.HttpConstants;
import com.bite.common.core.controller.BaseController;
import com.bite.common.core.domain.R;
import com.bite.common.core.domain.TableDataInfo;
import com.bite.friend.domain.exam.dto.ExamDTO;
import com.bite.friend.domain.exam.dto.ExamQueryDTO;
import com.bite.friend.service.user.IUserExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/exam")
public class UserExamController extends BaseController {

    @Autowired
    private IUserExamService userExamService;



    /*
     * 竞赛报名
     */
    @PostMapping("/enter")
    public R<Void> enter(@RequestHeader(HttpConstants.AUTHENTICATION) String token, @RequestBody ExamDTO examDTO){
        return toR(userExamService.enter(token, examDTO.getExamId()));
    }

    /*
     * 我的竞赛
     */
    @GetMapping("/list")
    public TableDataInfo list(ExamQueryDTO examQueryDTO){
        return userExamService.list(examQueryDTO);
    }


}
