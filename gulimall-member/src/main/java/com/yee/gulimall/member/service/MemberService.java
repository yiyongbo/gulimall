package com.yee.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yee.common.utils.PageUtils;
import com.yee.gulimall.member.entity.MemberEntity;
import com.yee.gulimall.member.exception.PhoneExistException;
import com.yee.gulimall.member.exception.UsernameExistException;
import com.yee.gulimall.member.vo.UserLoginVO;
import com.yee.gulimall.member.vo.UserRegisterVO;

import java.util.Map;

/**
 * 会员
 *
 * @author yee
 * @email yyb990929@gmail.com
 * @date 2021-12-08 23:12:00
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(UserRegisterVO userRegisterVO);

    void checkPhoneUnique(String phone) throws PhoneExistException;

    void checkUsernameUnique(String username) throws UsernameExistException;

    MemberEntity login(UserLoginVO userLoginVO);
}

