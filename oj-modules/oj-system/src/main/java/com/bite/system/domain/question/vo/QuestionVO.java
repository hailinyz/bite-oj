package com.bite.system.domain.question.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class QuestionVO {

    @JsonSerialize(using = ToStringSerializer.class) // 解决Long精度丢失问题
    private Long questionId;

    private String title;

    private Integer difficulty;

    private String createName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // 格式化时间
    private LocalDateTime createTime;


}
