package com.yee.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yee.gulimall.product.entity.SpuInfoEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yee.common.utils.PageUtils;
import com.yee.common.utils.Query;

import com.yee.gulimall.product.dao.SkuInfoDao;
import com.yee.gulimall.product.entity.SkuInfoEntity;
import com.yee.gulimall.product.service.SkuInfoService;
import org.springframework.util.StringUtils;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        String key = (String) params.get("key");
        String min = (String) params.get("min");
        String max = (String) params.get("max");
        String brandId = (String) params.get("brandId");
        String catalogId = (String) params.get("catelogId");

        boolean isMax = StringUtils.hasText(max) && (new BigDecimal(max).compareTo(BigDecimal.ZERO) == 1);
        LambdaQueryWrapper<SkuInfoEntity> queryWrapper = Wrappers.lambdaQuery(SkuInfoEntity.class);
        if (StringUtils.hasText(key)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq(SkuInfoEntity::getSkuId, key).or().like(SkuInfoEntity::getSkuName, key);
            });
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
                        .eq(StringUtils.hasText(brandId) && !"0".equalsIgnoreCase(brandId), SkuInfoEntity::getBrandId, brandId)
                        .eq(StringUtils.hasText(catalogId) && !"0".equalsIgnoreCase(catalogId), SkuInfoEntity::getCatalogId, catalogId)
                        .ge(StringUtils.hasText(min), SkuInfoEntity::getPrice, min)
                        .le(isMax, SkuInfoEntity::getPrice, max)
        );
        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        return this.list(
                Wrappers.lambdaQuery(SkuInfoEntity.class).eq(SkuInfoEntity::getSpuId, spuId)
        );
    }

}