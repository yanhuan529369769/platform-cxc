package com.jrzx.platform.cxc.common.web.handler;

import com.jr.platform.core.api.Result;
import com.jr.platform.core.api.ResultCode;
import com.jr.platform.core.exception.BaseException;
import com.jr.platform.core.exception.TokenException;
import com.jr.platform.core.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;

/**
 * Springboot WEB应用全局异常处理
 */
@Slf4j
@ResponseBody
@RestControllerAdvice
public class BaseExceptionHandler {

    /**
     * BaseException 异常捕获处理
     * @param ex 自定义BaseException异常类型
     * @return Result
     */
    @ExceptionHandler(value = BaseException.class)
    public Result<?> handleException(BaseException ex, HttpServletRequest request) {
        log.error(errorMessage(ex), ex);
        return Result.failed().code(ResultCode.ERROR.getCode()).msg(ex.getMessage()).path(request.getRequestURI()).httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    /**
     * TokenException 异常捕获处理
     * @param ex 自定义TokenException异常类型
     * @return Result
     */
    @ExceptionHandler(value = TokenException.class)
    public Result<?> handleException(TokenException ex, HttpServletRequest request) {
        log.error("TokenException:", ex);
        return Result.failed().code(ResultCode.NO_AUTHENTICATION.getCode()).msg(ex.getMessage()).path(request.getRequestURI()).httpStatus(HttpStatus.UNAUTHORIZED.value());
    }

    /**
     * FileNotFoundException,NoHandlerFoundException 异常捕获处理
     * @param ex 自定义FileNotFoundException异常类型
     * @return Result
     */
    @ExceptionHandler({FileNotFoundException.class, NoHandlerFoundException.class})
    public Result<?> noFoundException(Exception ex,HttpServletRequest request) {
        log.error("FileNotFoundException:", ex);
        return Result.failed().code(ResultCode.NOT_FOUND.getCode()).msg(ex.getMessage()).path(request.getRequestURI()).httpStatus(HttpStatus.NOT_FOUND.value());
    }

    /**
     * NullPointerException 空指针异常捕获处理
     * @param ex 自定义NullPointerException异常类型
     * @return Result
     */
    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(NullPointerException ex,HttpServletRequest request) {
        String message=ex.getMessage();
        log.error("NullPointerException:", ex);
        if(StringUtil.isBlank(message)){
            message="对象为空！";
        }
        return Result.failed().code(ResultCode.ERROR.getCode()).msg(message).path(request.getRequestURI()).httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }


    public String errorMessage(BaseException e) {
        return e.getCode() + ":" + e.getLocalizedMessage();
    }

    /**
     * 通用Exception异常捕获
     * @param ex 自定义Exception异常类型
     * @return Result
     */
    @ExceptionHandler(value = Exception.class)
    public Result<?> handleException(Exception ex,HttpServletRequest request) {
        log.error("程序异常：" + ex.toString());
        String message = ex.getMessage();
        if (StringUtils.contains(message, "Bad credentials")){
            message = "您输入的密码不正确";
        } else if (StringUtils.contains(ex.toString(), "InternalAuthenticationServiceException")) {
            message = "您输入的用户名不存在";
        }
        return Result.failed().code(ResultCode.ERROR.getCode()).msg(message).path(request.getRequestURI()).httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}