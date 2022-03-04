package com.yee.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yee.common.utils.PageUtils;
import com.yee.common.utils.Query;
import com.yee.common.utils.R;
import com.yee.gulimall.ware.dao.WareSkuDao;
import com.yee.gulimall.ware.entity.WareSkuEntity;
import com.yee.gulimall.ware.feign.ProductFeignService;
import com.yee.gulimall.ware.service.WareSkuService;
import com.yee.gulimall.ware.vo.SkuHasStockVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String skuId = (String) params.get("skuId");
        String wareId = (String) params.get("wareId");
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                Wrappers.lambdaQuery(WareSkuEntity.class).eq(StringUtils.hasText(skuId), WareSkuEntity::getSkuId, skuId)
                        .eq(StringUtils.hasText(wareId), WareSkuEntity::getWareId, wareId)
        );

        return new PageUtils(page);
    }

    @Override
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        // 1、判断如果还没有这个库存记录新增
        List<WareSkuEntity> wareSkuEntities = this.baseMapper.selectList(
                Wrappers.lambdaQuery(WareSkuEntity.class)
                        .eq(WareSkuEntity::getSkuId, skuId)
                        .eq(WareSkuEntity::getWareId, wareId)
        );
        if (CollectionUtils.isEmpty(wareSkuEntities)) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            // 远程查询sku的名字，如果失败，整个事务无需回滚
            // 1、自己catch异常
            // TODO 还可以用什么办法让异常出现以后不回滚？高级
            try {
                R info = productFeignService.info(skuId);
                if (info.getCode() == 0) {
                    Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");
                    wareSkuEntity.setSkuName((String) data.get("skuName"));
                }
            } catch (Exception e) {

            }
            this.baseMapper.insert(wareSkuEntity);
        } else {
            this.baseMapper.addStock(skuId, wareId, skuNum);
        }

    }

    @Override
    public List<SkuHasStockVO> getSkusHasStock(List<Long> skuIds) {
        List<WareSkuEntity> wareSkuEntities = this.list(Wrappers.lambdaQuery(WareSkuEntity.class)
                .in(WareSkuEntity::getSkuId, skuIds));
        List<SkuHasStockVO> collect = wareSkuEntities.stream().map(item -> {
            SkuHasStockVO skuHasStockVO = new SkuHasStockVO();
            skuHasStockVO.setSkuId(item.getSkuId());
            skuHasStockVO.setHasStock((item.getStock() - item.getStockLocked()) > 0);
            return skuHasStockVO;
        }).collect(Collectors.toList());
        return collect;
    }

}