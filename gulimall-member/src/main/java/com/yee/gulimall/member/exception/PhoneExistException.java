package com.yee.gulimall.member.exception;

/**
 * @author YYB
 */
public class PhoneExistException extends RuntimeException {

    public PhoneExistException() {
        super("手机号存在");
    }
}
