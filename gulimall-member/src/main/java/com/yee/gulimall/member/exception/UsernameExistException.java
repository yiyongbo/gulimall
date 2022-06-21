package com.yee.gulimall.member.exception;

/**
 * @author YYB
 */
public class UsernameExistException extends RuntimeException {

    public UsernameExistException() {
        super("用户名存在");
    }
}
