package com.yee.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码和错误信息定义类
 * 1、错误码定义规则为5为数字
 * 2、前两位表示业务场景，最后三位表示错误码。例如：10001。10：通用 001：系统未知异常
 * 3、维护错误码后需要维护错误描述，将它们定义为枚举形式
 * 错误码列表：
 *  10：通用
 *      001：参数格式校验
 *      002：短信验证码获取频率太高，请稍后再试
 *  11：商品
 *  12：订单
 *  13：购物车
 *  14：物流
 *  15：用户
 *      001：用户已存在
 *      002：手机号已存在
 *      003：账号或密码错误
 *
 *
 * @author YYB
 */
@Getter
@AllArgsConstructor
public enum BizCodeEnum {

    /**
     * 10000, "系统未知异常"
     */
    UNKNOWN_EXCEPTION(10000, "系统未知异常"),
    /**
     * 10001, "参数格式校验失败"
     */
    VALID_EXCEPTION(10001, "参数格式校验失败"),
    /**
     * 10002, "短信验证码获取频率太高，请稍后再试"
     */
    SMS_CODE_EXCEPTION(10002, "短信验证码获取频率太高，请稍后再试"),
    /**
     * 11000, "商品上架异常"
     */
    PRODUCT_UP_EXCEPTION(11000, "商品上架异常"),
    /**
     * 15001, "用户已存在"
     */
    USER_EXIST_EXCEPTION(15001, "用户已存在"),
    /**
     * 15002, "手机号已存在"
     */
    PHONE_EXIST_EXCEPTION(15002, "手机号已存在"),
    /**
     * 15003, "账号或密码错误"
     */
    LOGIN_ACCOUNT_PASSWORD_INVALID_EXCEPTION(15003, "账号或密码错误");

    private final Integer code;
    private final String msg;



}
