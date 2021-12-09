package com.yee.gulimall.order.controller;

import com.yee.common.utils.PageUtils;
import com.yee.common.utils.R;
import com.yee.gulimall.order.entity.OrderOperateHistoryEntity;
import com.yee.gulimall.order.service.OrderOperateHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;



/**
 * 订单操作历史记录
 *
 * @author yee
 * @email yyb990929@gmail.com
 * @date 2021-12-08 23:12:45
 */
@RestController
@RequestMapping("order/orderoperatehistory")
public class OrderOperateHistoryController {
    @Autowired
    private OrderOperateHistoryService orderOperateHistoryService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("order:omsorderoperatehistory:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = orderOperateHistoryService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("order:omsorderoperatehistory:info")
    public R info(@PathVariable("id") Long id){
		OrderOperateHistoryEntity omsOrderOperateHistory = orderOperateHistoryService.getById(id);

        return R.ok().put("omsOrderOperateHistory", omsOrderOperateHistory);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("order:omsorderoperatehistory:save")
    public R save(@RequestBody OrderOperateHistoryEntity omsOrderOperateHistory){
		orderOperateHistoryService.save(omsOrderOperateHistory);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("order:omsorderoperatehistory:update")
    public R update(@RequestBody OrderOperateHistoryEntity omsOrderOperateHistory){
		orderOperateHistoryService.updateById(omsOrderOperateHistory);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("order:omsorderoperatehistory:delete")
    public R delete(@RequestBody Long[] ids){
		orderOperateHistoryService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
