package com.yee.gulimall.member.controller;

import com.yee.common.utils.PageUtils;
import com.yee.common.utils.R;
import com.yee.gulimall.member.entity.GrowthChangeHistoryEntity;
import com.yee.gulimall.member.service.GrowthChangeHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;



/**
 * 成长值变化历史记录
 *
 * @author yee
 * @email yyb990929@gmail.com
 * @date 2021-12-08 23:12:00
 */
@RestController
@RequestMapping("member/growthchangehistory")
public class GrowthChangeHistoryController {
    @Autowired
    private GrowthChangeHistoryService growthChangeHistoryService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("member:umsgrowthchangehistory:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = growthChangeHistoryService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("member:umsgrowthchangehistory:info")
    public R info(@PathVariable("id") Long id){
		GrowthChangeHistoryEntity umsGrowthChangeHistory = growthChangeHistoryService.getById(id);

        return R.ok().put("umsGrowthChangeHistory", umsGrowthChangeHistory);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("member:umsgrowthchangehistory:save")
    public R save(@RequestBody GrowthChangeHistoryEntity umsGrowthChangeHistory){
        growthChangeHistoryService.save(umsGrowthChangeHistory);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("member:umsgrowthchangehistory:update")
    public R update(@RequestBody GrowthChangeHistoryEntity umsGrowthChangeHistory){
        growthChangeHistoryService.updateById(umsGrowthChangeHistory);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("member:umsgrowthchangehistory:delete")
    public R delete(@RequestBody Long[] ids){
        growthChangeHistoryService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
