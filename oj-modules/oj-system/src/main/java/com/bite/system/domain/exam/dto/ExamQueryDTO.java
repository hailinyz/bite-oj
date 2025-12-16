package com.bite.system.domain.exam.dto;

import com.bite.common.core.domain.PageQueryDTO;
import lombok.Getter;
import lombok.Setter;


import java.util.Set;

//属性就是发起请求时候前端会传递什么参数
@Getter
@Setter
public class ExamQueryDTO extends PageQueryDTO {

    private String title;

    private String startTime;

/*    private String excludeIdStr;

    private Set<Long> excludeIdSet;*/

    private String endTime;
}
