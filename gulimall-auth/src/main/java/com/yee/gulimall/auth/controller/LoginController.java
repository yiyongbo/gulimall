package com.yee.gulimall.auth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.yee.common.constant.AuthConstant;
import com.yee.common.exception.BizCodeEnum;
import com.yee.common.utils.R;
import com.yee.gulimall.auth.feign.MemberFeignService;
import com.yee.gulimall.auth.feign.ThirdPartFeignService;
import com.yee.gulimall.auth.vo.UserLoginVO;
import com.yee.gulimall.auth.vo.UserRegisterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author YYB
 */
public class LoginController {

    @Autowired
    ThirdPartFeignService thirdPartFeignService;

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * 发送一个请求直接跳转到一个页面
     * SpringMVC viewController：将请求和页面映射过来
     */

    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone) {
        // TODO 1、接口防刷

        String redisCode = stringRedisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (StringUtils.hasText(redisCode)) {
            long l = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - l < 60000) {
                // 60s内不能再发
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION);
            }
        }

        // 2、验证码的再次校验，redis。存key-phone，value-code，sms:code:phone -> code
        String code = UUID.randomUUID().toString().substring(0, 5);
        System.out.println("============验证码=========》》》" + code);
        // redis缓存验证码，防止同一个phone在60秒内再次发送验证码
        stringRedisTemplate.opsForValue()
                .set(AuthConstant.SMS_CODE_CACHE_PREFIX + phone, code + "_" + System.currentTimeMillis(), 5, TimeUnit.MINUTES);

        // 测试请使用默认模板：【创信】你的验证码是：5873，3分钟内有效！；测试套餐次数已用完
        // thirdPartFeignService.sendCode(phone, code);
        return R.ok();
    }

    /**
     * // TODO 重定向携带数据，利用session原理。将数据放在session中，只要跳到下一个页面取出这个数据以后，session里面的数据就会删掉
     * // TODO 1、分布式下的session问题。
     * @param userRegisterVO
     * @param bindingResult
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/register")
    public String register(@Valid UserRegisterVO userRegisterVO, BindingResult bindingResult, RedirectAttributes redirectAttributes) throws JsonProcessingException {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors", errors);
            // 校验出错，转发到注册页
            return "redirect:http://auth.gulimall.com/reg.html";
        }

        // 真正注册。调用远程
        // 1、校验验证码
        String redisCode = stringRedisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + userRegisterVO.getPhone());
        String code = userRegisterVO.getCode();
        if (StringUtils.hasText(redisCode)) {
            if (code.equals(redisCode.split("_")[0])) {
                // 删除验证码
                stringRedisTemplate.delete(AuthConstant.SMS_CODE_CACHE_PREFIX + userRegisterVO.getPhone());
                // 验证码通过，调用远程服务进行注册。
                R r = memberFeignService.register(userRegisterVO);
                if (r.getCode() == 0) {
                    // 成功
                    return "redirect:http://auth.gulimall.com/login.html";
                } else {
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", r.getData("msg", new TypeReference<String>() {}));
                    redirectAttributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.gulimall.com/reg.html";
                }
            } else {
                Map<String, String> errors = new HashMap<>();
                errors.put("code", "验证码错误");
                redirectAttributes.addFlashAttribute("errors", errors);
                // 校验出错，转发到注册页
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码失效");
            redirectAttributes.addFlashAttribute("errors", errors);
            // 校验出错，转发到注册页
            return "redirect:http://auth.gulimall.com/reg.html";
        }
    }

    @PostMapping("/login")
    public String login(UserLoginVO userLoginVO, RedirectAttributes redirectAttributes) throws JsonProcessingException {
        // 远程登录
        R r = memberFeignService.login(userLoginVO);
        if (r.getCode() == 0) {
            // TODO 1、登录成功处理
            return "redirect:http://gulimall.com";
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", r.getData("msg", new TypeReference<String>() {}));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
    }


}
