package com.yee.gulimall.ware.feign;

import com.yee.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author YYB
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    /**
     *
     * 1、让所有请求过网关
     *  1、@FeignClient("gulimall-gateway")：给gulimall-gateway所在机器发请求
     *  2、/api/product/skuinfo/info/{skuId}
     * 2、直接让后台指定服务处理
     *  1、@FeignClient("gulimall-product")
     *  2、/product/skuinfo/info/{skuId}
     *
     * @param skuId skuId
     * @return
     */
    @GetMapping("/product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);
}
