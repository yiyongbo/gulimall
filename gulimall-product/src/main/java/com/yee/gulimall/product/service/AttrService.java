package com.yee.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yee.common.utils.PageUtils;
import com.yee.gulimall.product.entity.AttrEntity;
import com.yee.gulimall.product.vo.AttrGroupRelationVO;
import com.yee.gulimall.product.vo.AttrResponseVO;
import com.yee.gulimall.product.vo.AttrVO;

import java.util.List;
import java.util.Map;

/**
 * 商品属性
 *
 * @author yee
 * @email yyb990929@gmail.com
 * @date 2021-12-08 21:53:20
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveAttr(AttrVO vo);

    PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type);

    AttrResponseVO getAttrInfo(Long attrId);

    void updateAttr(AttrVO vo);

    List<AttrEntity> getRelationAttr(Long attrgroundId);

    void deleteRelation(AttrGroupRelationVO[] vos);

    PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId);
}

