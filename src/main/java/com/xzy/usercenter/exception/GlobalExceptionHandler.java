package com.xzy.usercenter.exception;


import com.xzy.usercenter.common.BaseResponse;
import com.xzy.usercenter.common.ErrorCode;
import com.xzy.usercenter.common.ResultUtil;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GlobalExceptionHandler {



    @ExceptionHandler(RuntimeException.class)
    public  BaseResponse<?> runtimeExceptionHandle(RuntimeException e) {
        return ResultUtil.error(ErrorCode.SYSTEM_ERROR,e.getMessage());
    }
}
