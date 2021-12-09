package com.yee.gulimall.order.controller;

import com.yee.common.utils.PageUtils;
import com.yee.common.utils.R;
import com.yee.gulimall.order.entity.OrderReturnReasonEntity;
import com.yee.gulimall.order.service.OrderReturnReasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;



/**
 * 退货原因
 *
 * @author yee
 * @email yyb990929@gmail.com
 * @date 2021-12-08 23:12:44
 */
@RestController
@RequestMapping("order/orderreturnreason")
public class OrderReturnReasonController {
    @Autowired
    private OrderReturnReasonService orderReturnReasonService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("order:omsorderreturnreason:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = orderReturnReasonService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("order:omsorderreturnreason:info")
    public R info(@PathVariable("id") Long id){
		OrderReturnReasonEntity omsOrderReturnReason = orderReturnReasonService.getById(id);

        return R.ok().put("omsOrderReturnReason", omsOrderReturnReason);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("order:omsorderreturnreason:save")
    public R save(@RequestBody OrderReturnReasonEntity omsOrderReturnReason){
		orderReturnReasonService.save(omsOrderReturnReason);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("order:omsorderreturnreason:update")
    public R update(@RequestBody OrderReturnReasonEntity omsOrderReturnReason){
		orderReturnReasonService.updateById(omsOrderReturnReason);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("order:omsorderreturnreason:delete")
    public R delete(@RequestBody Long[] ids){
		orderReturnReasonService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
