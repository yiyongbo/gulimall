package com.yee.gulimall.member.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * @author YYB
 */
@Data
public class UserRegisterVO {

    private String username;

    private String password;

    private String phone;

}
