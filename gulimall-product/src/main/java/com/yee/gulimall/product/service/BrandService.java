package com.yee.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yee.common.utils.PageUtils;
import com.yee.gulimall.product.entity.BrandEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌
 *
 * @author yee
 * @email yyb990929@gmail.com
 * @date 2021-12-08 21:53:20
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void updateDetail(BrandEntity brand);

    List<BrandEntity> getBrandsByIds(List<Long> brandIds);
}

