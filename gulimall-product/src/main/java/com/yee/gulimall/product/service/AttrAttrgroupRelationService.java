package com.yee.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yee.common.utils.PageUtils;
import com.yee.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.yee.gulimall.product.vo.AttrGroupRelationVO;

import java.util.List;
import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author yee
 * @email yyb990929@gmail.com
 * @date 2021-12-08 21:53:20
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveBatch(List<AttrGroupRelationVO> vos);
}

