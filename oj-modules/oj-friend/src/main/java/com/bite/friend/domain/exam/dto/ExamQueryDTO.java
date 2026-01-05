package com.bite.friend.domain.exam.dto;

import com.bite.common.core.domain.PageQueryDTO;
import lombok.Getter;
import lombok.Setter;

//属性就是发起请求时候前端会传递什么参数
@Getter
@Setter
public class ExamQueryDTO extends PageQueryDTO {

    private String title;

    private String startTime;

/*    private String excludeIdStr;

    private Set<Long> excludeIdSet;*/

    private String endTime;

    private Integer type; //0 未完赛 1 历史竞赛 2 我的竞赛

}
