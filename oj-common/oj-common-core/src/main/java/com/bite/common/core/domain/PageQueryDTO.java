package com.bite.common.core.domain;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageQueryDTO {

    private Integer pageSize = 10; // 每页显示的条数

    private Integer pageNum = 1; // 当前页码

}
