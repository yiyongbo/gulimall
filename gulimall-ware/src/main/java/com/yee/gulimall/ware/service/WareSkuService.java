package com.yee.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yee.common.utils.PageUtils;
import com.yee.gulimall.ware.entity.WareSkuEntity;
import com.yee.gulimall.ware.vo.SkuHasStockVO;

import java.util.List;
import java.util.Map;

/**
 * εεεΊε­
 *
 * @author yee
 * @email yyb990929@gmail.com
 * @date 2021-12-08 23:14:19
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVO> getSkusHasStock(List<Long> skuIds);
}

