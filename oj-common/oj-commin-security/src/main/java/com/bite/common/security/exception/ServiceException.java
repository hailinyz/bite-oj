package com.bite.common.security.exception;

import com.bite.common.core.enums.ResultCode;
import lombok.Getter;

/*
* 自定义异常
 */
@Getter
public class ServiceException extends RuntimeException{

    private ResultCode resultCode;

    public ServiceException(ResultCode resultCode) {
        this.resultCode = resultCode;
    }

}
