package com.bite.system.domain.exam.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
public class ExamQuestionAddDTO {

    private Long examId;

    private LinkedHashSet<Long> questionIdSet; //之前用的是 Set,但是添加题目的时候，顺序很重要，所以用LinkedHashSet

}
