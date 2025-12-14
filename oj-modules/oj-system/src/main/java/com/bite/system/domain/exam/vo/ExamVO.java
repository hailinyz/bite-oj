package com.bite.system.domain.exam.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

//前端要展示的属性
@Getter
@Setter
public class ExamVO {

    private String title;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private String createName;

    private LocalDateTime createTime;

}
