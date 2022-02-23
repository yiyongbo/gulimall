package com.yee.gulimall.product.controller;

import java.util.Arrays;
import java.util.Map;

import com.yee.gulimall.product.vo.AttrResponseVO;
import com.yee.gulimall.product.vo.AttrVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.yee.gulimall.product.entity.AttrEntity;
import com.yee.gulimall.product.service.AttrService;
import com.yee.common.utils.PageUtils;
import com.yee.common.utils.R;



/**
 * 商品属性
 *
 * @author yee
 * @email yyb990929@gmail.com
 * @date 2021-12-08 22:20:12
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    @GetMapping("/{attrType}/list/{catelogId}")
    // @RequiresPermissions("product:attr:list")
    public R baseAttrList(@RequestParam Map<String, Object> params,
                          @PathVariable("catelogId") Long catelogId,
                          @PathVariable("attrType") String type){
        PageUtils page = attrService.queryBaseAttrPage(params, catelogId, type);

        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    // @RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId){
		// AttrEntity attr = attrService.getById(attrId);
        AttrResponseVO attrResponseVO = attrService.getAttrInfo(attrId);

        return R.ok().put("attr", attrResponseVO);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrVO vo){
		attrService.saveAttr(vo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrVO vo){
		attrService.updateAttr(vo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}
