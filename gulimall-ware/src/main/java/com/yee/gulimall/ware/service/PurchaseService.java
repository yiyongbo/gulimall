package com.yee.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yee.common.utils.PageUtils;
import com.yee.gulimall.ware.entity.PurchaseEntity;
import com.yee.gulimall.ware.vo.MergeVO;
import com.yee.gulimall.ware.vo.PurchaseDoneVO;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author yee
 * @email yyb990929@gmail.com
 * @date 2021-12-08 23:14:19
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageUnreceivePurchase(Map<String, Object> params);

    void mergePurchase(MergeVO vo);

    void received(List<Long> ids);

    void done(PurchaseDoneVO vo);
}

