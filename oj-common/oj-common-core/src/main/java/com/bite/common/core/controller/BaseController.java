package com.bite.common.core.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.bite.common.core.domain.R;
import com.bite.common.core.domain.TableDataInfo;
import com.github.pagehelper.PageInfo;

import java.util.List;

public class BaseController {

    public R<Void> toR(int rows){
        return rows > 0 ? R.ok() : R.fail();
    }

    public R<Void> toR(boolean result){
        return result ? R.ok() : R.fail();
    }

    public TableDataInfo getTableDataInfo(List<?> list){
        if (CollectionUtil.isEmpty(list)){ //使用hutool工具包判断集合是否为空
            return TableDataInfo.empty(); //未查出任何数据时调用
        }
        return TableDataInfo.success(list, new PageInfo<>(list).getTotal());
    }

}
