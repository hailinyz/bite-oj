package com.bite.system.domain.exam.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExamEditDTO extends ExamAddDTO{

    //为了防止截断加上注解
    @JsonSerialize(using = ToStringSerializer.class)
    private Long examId;
}
