package com.yee.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yee.common.utils.PageUtils;
import com.yee.common.utils.Query;
import com.yee.gulimall.ware.dao.PurchaseDetailDao;
import com.yee.gulimall.ware.entity.PurchaseDetailEntity;
import com.yee.gulimall.ware.service.PurchaseDetailService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");
        String status = (String) params.get("status");
        String wareId = (String) params.get("wareId");
        LambdaQueryWrapper<PurchaseDetailEntity> queryWrapper = Wrappers.lambdaQuery(PurchaseDetailEntity.class);
        if (StringUtils.hasText(key)) {
            queryWrapper.and(wrapper -> {
                wrapper.eq(PurchaseDetailEntity::getPurchaseId, key).or().eq(PurchaseDetailEntity::getSkuId, key);
            });
        }
        queryWrapper.eq(StringUtils.hasText(status), PurchaseDetailEntity::getStatus, status)
                .eq(StringUtils.hasText(wareId), PurchaseDetailEntity::getWareId, wareId);

        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

}