package com.yee.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yee.common.utils.PageUtils;
import com.yee.common.utils.Query;
import com.yee.gulimall.product.dao.SkuInfoDao;
import com.yee.gulimall.product.entity.SkuImagesEntity;
import com.yee.gulimall.product.entity.SkuInfoEntity;
import com.yee.gulimall.product.entity.SpuInfoDescEntity;
import com.yee.gulimall.product.service.*;
import com.yee.gulimall.product.vo.SkuItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<>()
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

    @Override
    public SkuItemVO item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVO skuItemVO = new SkuItemVO();

        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            // 1、sku基本信息获取  pms_sku_info
            SkuInfoEntity skuInfoEntity = getById(skuId);
            skuItemVO.setInfo(skuInfoEntity);
            return skuInfoEntity;
        }, executor);

        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            // 3、获取spu的销售属性组合
            List<SkuItemVO.SkuItemSaleAttrVO> saleAttrVOS = skuSaleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            skuItemVO.setSaleAttr(saleAttrVOS);
        }, executor);

        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((res) -> {
            // 4、获取spu的介绍
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(res.getSpuId());
            skuItemVO.setDesp(spuInfoDescEntity);
        }, executor);

        CompletableFuture<Void> baseAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            // 5、获取spu的规格参数信息
            List<SkuItemVO.SpuItemAttrGroupVO> attrGroupVOS = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVO.setGroupAttrs(attrGroupVOS);
        }, executor);

        CompletableFuture<Void>  imageFuture = CompletableFuture.runAsync(() -> {
            // 2、sku的图片信息 pms_sku_images
            List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);
            skuItemVO.setImages(images);
        }, executor);

        // 等到所有任务都完成
        CompletableFuture.allOf(infoFuture, saleAttrFuture, descFuture, baseAttrFuture, imageFuture).get();

        return skuItemVO;
    }

}