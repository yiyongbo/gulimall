package com.yee.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.yee.gulimall.product.entity.AttrEntity;
import com.yee.gulimall.product.service.AttrAttrgroupRelationService;
import com.yee.gulimall.product.service.AttrService;
import com.yee.gulimall.product.service.CategoryService;
import com.yee.gulimall.product.vo.AttrGroupRelationVO;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.yee.gulimall.product.entity.AttrGroupEntity;
import com.yee.gulimall.product.service.AttrGroupService;
import com.yee.common.utils.PageUtils;
import com.yee.common.utils.R;



/**
 * 属性分组
 *
 * @author yee
 * @email yyb990929@gmail.com
 * @date 2021-12-08 22:20:12
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    AttrService attrService;

    @Autowired
    AttrAttrgroupRelationService attrAttrgroupRelationService;

    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrGroupRelationVO> vos) {
        attrAttrgroupRelationService.saveBatch(vos);
        return R.ok();
    }

    @GetMapping("/{attrgroundId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroundId") Long attrgroundId) {
        List<AttrEntity> attrEntityList = attrService.getRelationAttr(attrgroundId);
        return R.ok().put("data", attrEntityList);
    }

    @GetMapping("/{attrgroupId}/noattr/relation")
    // @RequiresPermissions("product:attrgroup:list")
    public R attrNoRelation(@RequestParam Map<String, Object> params, @PathVariable("attrgroupId") Long attrgroupId){
        PageUtils page = attrService.getNoRelationAttr(params, attrgroupId);
        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    // @RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params, @PathVariable("catelogId") Long catelogId){
        PageUtils page = attrGroupService.queryPage(params, catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    // @RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        Long catelogId = attrGroup.getCatelogId();
        Long[] catelogPath = categoryService.findCatelogPath(catelogId);
        attrGroup.setCatelogPath(catelogPath);

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVO[] vos) {
        attrService.deleteRelation(vos);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
