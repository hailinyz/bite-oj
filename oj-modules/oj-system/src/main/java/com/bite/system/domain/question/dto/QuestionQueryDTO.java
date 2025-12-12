package com.bite.system.domain.question.dto;

import com.bite.common.core.domain.PageQueryDTO;
import lombok.*;

@Getter
@Setter
public class QuestionQueryDTO extends PageQueryDTO {

    private Integer difficulty;

    private String title;


}
