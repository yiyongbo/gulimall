package com.yee.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yee.common.utils.PageUtils;
import com.yee.gulimall.product.entity.SpuCommentEntity;

import java.util.Map;

/**
 * 商品评价
 *
 * @author yee
 * @email yyb990929@gmail.com
 * @date 2021-12-08 21:53:20
 */
public interface SpuCommentService extends IService<SpuCommentEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

