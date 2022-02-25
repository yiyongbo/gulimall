package com.yee.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yee.common.utils.PageUtils;
import com.yee.common.utils.Query;
import com.yee.gulimall.ware.dao.WareInfoDao;
import com.yee.gulimall.ware.entity.WareInfoEntity;
import com.yee.gulimall.ware.service.WareInfoService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                Wrappers.lambdaQuery(WareInfoEntity.class).and(StringUtils.hasText(key), wrapper -> {
                    wrapper.eq(WareInfoEntity::getId, key).or().like(WareInfoEntity::getName, key);
                })
        );

        return new PageUtils(page);
    }

}