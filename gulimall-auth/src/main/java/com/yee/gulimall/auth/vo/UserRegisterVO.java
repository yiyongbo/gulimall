package com.yee.gulimall.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @author YYB
 */
@Data
public class UserRegisterVO {

    @NotBlank(message = "用户名必须填写")
    @Length(min = 6, max = 18, message = "用户名必须是6-18位字符")
    private String username;

    @NotBlank(message = "密码必须填写")
    @Length(min = 6, max = 18, message = "密码必须是6-18位字符")
    private String password;

    @NotBlank(message = "手机号必须填写")
    @Pattern(regexp = "^[1](3-9)[0-9]{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "验证码必须填写")
    private String code;
}
