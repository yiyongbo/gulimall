package com.yee.gulimall.ware.controller;

import com.yee.common.utils.PageUtils;
import com.yee.common.utils.R;
import com.yee.gulimall.ware.entity.PurchaseEntity;
import com.yee.gulimall.ware.service.PurchaseService;
import com.yee.gulimall.ware.vo.MergeVO;
import com.yee.gulimall.ware.vo.PurchaseDoneVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;



/**
 * 采购信息
 *
 * @author yee
 * @email yyb990929@gmail.com
 * @date 2021-12-08 23:14:19
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;


    @PostMapping("/done")
    public R done(@RequestBody PurchaseDoneVO vo){
        purchaseService.done(vo);
        return R.ok();
    }

    @PostMapping("/received")
    public R received(@RequestBody List<Long> ids){
        purchaseService.received(ids);
        return R.ok();
    }

    @PostMapping("/merge")
    public R merge(@RequestBody MergeVO vo){
        purchaseService.mergePurchase(vo);
        return R.ok();
    }

    @RequestMapping("/unreceive/list")
    public R unreceiveList(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPageUnreceivePurchase(params);
        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("ware:wmspurchase:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("ware:wmspurchase:info")
    public R info(@PathVariable("id") Long id){
		PurchaseEntity wmsPurchase = purchaseService.getById(id);

        return R.ok().put("wmsPurchase", wmsPurchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("ware:wmspurchase:save")
    public R save(@RequestBody PurchaseEntity wmsPurchase){
        wmsPurchase.setCreateTime(new Date());
        wmsPurchase.setUpdateTime(new Date());
		purchaseService.save(wmsPurchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("ware:wmspurchase:update")
    public R update(@RequestBody PurchaseEntity wmsPurchase){
		purchaseService.updateById(wmsPurchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("ware:wmspurchase:delete")
    public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
