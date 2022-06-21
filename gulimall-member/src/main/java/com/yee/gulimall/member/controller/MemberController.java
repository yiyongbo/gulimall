package com.yee.gulimall.member.controller;

import com.yee.common.exception.BizCodeEnum;
import com.yee.common.utils.PageUtils;
import com.yee.common.utils.R;
import com.yee.gulimall.member.entity.MemberEntity;
import com.yee.gulimall.member.exception.PhoneExistException;
import com.yee.gulimall.member.exception.UsernameExistException;
import com.yee.gulimall.member.feign.CouponFeignService;
import com.yee.gulimall.member.service.MemberService;
import com.yee.gulimall.member.vo.UserLoginVO;
import com.yee.gulimall.member.vo.UserRegisterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;



/**
 * 会员
 *
 * @author yee
 * @email yyb990929@gmail.com
 * @date 2021-12-08 23:12:00
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;
    @Autowired
    private CouponFeignService couponFeignService;

    @RequestMapping("/coupons")
    public R test() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");

        R memberCoupons = couponFeignService.memberCoupons();

        return R.ok().put("member", memberEntity).put("coupons", memberCoupons.get("coupons"));
    }


    @PostMapping("/login")
    public R login(@RequestBody UserLoginVO userLoginVO) {
        MemberEntity memberEntity = memberService.login(userLoginVO);
        if (memberEntity != null) {
            return R.ok().setData(memberEntity);
        } else {
            return R.error(BizCodeEnum.LOGIN_ACCOUNT_PASSWORD_INVALID_EXCEPTION);
        }
    }


    @PostMapping("/register")
    public R register(@RequestBody UserRegisterVO userRegisterVO) {
        try {
            memberService.register(userRegisterVO);
        } catch (PhoneExistException e) {
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION);
        } catch (UsernameExistException e) {
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION);
        }
        return R.ok();
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("member:umsmember:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("member:umsmember:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity umsMember = memberService.getById(id);

        return R.ok().put("umsMember", umsMember);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("member:umsmember:save")
    public R save(@RequestBody MemberEntity umsMember){
		memberService.save(umsMember);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("member:umsmember:update")
    public R update(@RequestBody MemberEntity umsMember){
		memberService.updateById(umsMember);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("member:umsmember:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
