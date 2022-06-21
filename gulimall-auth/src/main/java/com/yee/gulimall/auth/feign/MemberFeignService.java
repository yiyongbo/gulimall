package com.yee.gulimall.auth.feign;

import com.yee.common.utils.R;
import com.yee.gulimall.auth.vo.UserLoginVO;
import com.yee.gulimall.auth.vo.UserRegisterVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author YYB
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/register")
    R register(@RequestBody UserRegisterVO userRegisterVO);

    @PostMapping("/member/member/login")
    public R login(@RequestBody UserLoginVO userLoginVO);
}
