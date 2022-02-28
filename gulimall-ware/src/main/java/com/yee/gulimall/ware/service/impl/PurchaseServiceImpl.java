package com.yee.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yee.common.constant.WareConstant;
import com.yee.common.utils.PageUtils;
import com.yee.common.utils.Query;
import com.yee.gulimall.ware.dao.PurchaseDao;
import com.yee.gulimall.ware.entity.PurchaseDetailEntity;
import com.yee.gulimall.ware.entity.PurchaseEntity;
import com.yee.gulimall.ware.service.PurchaseDetailService;
import com.yee.gulimall.ware.service.PurchaseService;
import com.yee.gulimall.ware.service.WareSkuService;
import com.yee.gulimall.ware.vo.MergeVO;
import com.yee.gulimall.ware.vo.PurchaseDoneVO;
import com.yee.gulimall.ware.vo.PurchaseItemDoneVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService purchaseDetailService;

    @Autowired
    WareSkuService wareSkuService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceivePurchase(Map<String, Object> params) {

        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                Wrappers.lambdaQuery(PurchaseEntity.class).eq(PurchaseEntity::getStatus,0).or()
                        .eq(PurchaseEntity::getStatus,1)
        );

        return new PageUtils(page);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void mergePurchase(MergeVO vo) {
        Long purchaseId = vo.getPurchaseId();
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getCode());
            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }

        // TODO 判断采购单状态

        List<Long> items = vo.getItems();
        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> purchaseDetailEntities = items.stream().map(item -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setId(item);
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());

            return purchaseDetailEntity;
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(purchaseDetailEntities);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void received(List<Long> ids) {
        // 1、确认当前采购单是新建或者已分配状态
        List<PurchaseEntity> purchaseEntities = this.list(
                Wrappers.lambdaQuery(PurchaseEntity.class)
                        .in(PurchaseEntity::getId, ids)
                        .and(w -> {
                            w.eq(PurchaseEntity::getStatus, WareConstant.PurchaseStatusEnum.CREATED.getCode())
                                    .or()
                                    .eq(PurchaseEntity::getStatus, WareConstant.PurchaseStatusEnum.ASSIGNED.getCode());
                        })
        );

        // 2、改变采购单的状态
        List<PurchaseEntity> collect = purchaseEntities.stream()
                .peek(item -> {
                    item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());
                    item.setUpdateTime(new Date());
                })
                .collect(Collectors.toList());
        this.updateBatchById(collect);

        // 3、改变采购项的状态
        List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.list(
                Wrappers.lambdaQuery(PurchaseDetailEntity.class).in(PurchaseDetailEntity::getPurchaseId, ids)
        );
        List<PurchaseDetailEntity> collect1 = purchaseDetailEntities.stream().peek(purchaseDetailEntity -> {
            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode());
        }).collect(Collectors.toList());
        purchaseDetailService.updateBatchById(collect1);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void done(PurchaseDoneVO vo) {
        Long purchaseId = vo.getId();

        // 1、改变采购项的状态
        boolean flag = true;
        List<PurchaseItemDoneVO> items = vo.getItems();
        List<PurchaseDetailEntity> updateItems = new ArrayList<>();
        for (PurchaseItemDoneVO item : items) {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            if (item.getStatus() == WareConstant.PurchaseDetailStatusEnum.HASERROR.getCode()) {
                flag = false;
            } else {
                PurchaseDetailEntity entity = purchaseDetailService.getById(item.getItemId());
                // 3、将成功的采购进行入库
                wareSkuService.addStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum());
            }
            purchaseDetailEntity.setStatus(item.getStatus());
            purchaseDetailEntity.setId(item.getItemId());
            updateItems.add(purchaseDetailEntity);
        }
        purchaseDetailService.updateBatchById(updateItems);

        // 2、改变采购单的状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setStatus(flag ? WareConstant.PurchaseStatusEnum.FINISH.getCode() : WareConstant.PurchaseStatusEnum.HASERROR.getCode());
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);


    }

}