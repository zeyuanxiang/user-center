package com.xzy.usercenter.common;

public class ResultUtil {

    public static  <T> BaseResponse<T> success(T data){
        return new BaseResponse<>(ErrorCode.SUCCESS.getCode(),"ok",data);
    }

    public static BaseResponse error(ErrorCode errorCode){
        return new BaseResponse(errorCode.getCode(),errorCode.getMessage(),null);
    }

    public static BaseResponse error(int code,String message){
        return new BaseResponse(code,message,null);
    }

    public static BaseResponse error(ErrorCode errorCode, String message){
        return new BaseResponse(errorCode.getCode(),message);
    }

}
