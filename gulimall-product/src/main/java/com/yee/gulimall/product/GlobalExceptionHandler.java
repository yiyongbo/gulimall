package com.yee.gulimall.product;

import com.yee.common.exception.BizCodeEnum;
import com.yee.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ValidationException;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理
 * @author YYB
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.yee.gulimall.product")
public class GlobalExceptionHandler {

    // @ExceptionHandler(value = {BindException.class, ValidationException.class, MethodArgumentNotValidException.class})
    // public R handleValidException(Exception e) {
    //     log.error("数据校验出现问题{}，异常类型：{}", e.getMessage(), e.getClass());
    //     return R.error();
    // }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e) {
        log.error("数据校验出现问题{}，异常类型：{}", e.getMessage(), e.getClass());
        BindingResult bindingResult = e.getBindingResult();

        Map<String, String> errorMap = new HashMap<>(16);
        bindingResult.getFieldErrors().forEach(item -> {
            errorMap.put(item.getField(), item.getDefaultMessage());
        });
        return R.error(BizCodeEnum.VALID_EXCEPTION).put("data", errorMap);
    }

    @ExceptionHandler(value = Exception.class)
    public R handleException(Exception e) {
        log.error("错误：", e);
        return R.error(BizCodeEnum.UNKNOWN_EXCEPTION);
    }


}
