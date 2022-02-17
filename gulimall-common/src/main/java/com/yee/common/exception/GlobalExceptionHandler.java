package com.yee.common.exception;

import com.yee.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ValidationException;

/**
 * 全局异常处理
 * @author YYB
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.yee.gulimall.product")
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {BindException.class, ValidationException.class, MethodArgumentNotValidException.class})
    public R handleValidException(Exception e) {
        log.error("数据校验出现问题{}，异常类型：{}", e.getMessage(), e.getClass());
        return R.error();
    }


}
