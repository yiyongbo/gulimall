package com.yee.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yee.common.utils.PageUtils;
import com.yee.common.utils.Query;
import com.yee.gulimall.product.dao.AttrGroupDao;
import com.yee.gulimall.product.entity.AttrEntity;
import com.yee.gulimall.product.entity.AttrGroupEntity;
import com.yee.gulimall.product.service.AttrGroupService;
import com.yee.gulimall.product.service.AttrService;
import com.yee.gulimall.product.vo.AttrGroupWithAttrsVO;
import com.yee.gulimall.product.vo.SkuItemVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        String key = (String) params.get("key");
        LambdaQueryWrapper<AttrGroupEntity> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.hasText(key)) {
            wrapper.and((obj) -> {
                obj.eq(AttrGroupEntity::getAttrGroupId, key).or().like(AttrGroupEntity::getAttrGroupName, key);
            });
        }
        if (catelogId == 0) {
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    wrapper
            );
            return new PageUtils(page);
        } else {
            wrapper.eq(AttrGroupEntity::getCatelogId, catelogId);
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    wrapper
            );
            return new PageUtils(page);
        }
    }

    /**
     * 根据分类id查出所有的分组以及这些组里面的属性
     * @param catelogId 分类id
     * @return
     */
    @Override
    public List<AttrGroupWithAttrsVO> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        // 1、查出当前分类下的所有属性分组
        List<AttrGroupEntity> attrGroupEntities = this.list(
                new QueryWrapper<AttrGroupEntity>().lambda().eq(AttrGroupEntity::getCatelogId, catelogId)
        );
        // 2、查询每个属性分组的所有属性
        List<AttrGroupWithAttrsVO> attrGroupWithAttrsVOS = attrGroupEntities.stream().map((item) -> {
            AttrGroupWithAttrsVO attrGroupWithAttrsVO = new AttrGroupWithAttrsVO();
            BeanUtils.copyProperties(item, attrGroupWithAttrsVO);
            List<AttrEntity> relationAttrs = attrService.getRelationAttr(item.getAttrGroupId());
            attrGroupWithAttrsVO.setAttrs(relationAttrs);
            return attrGroupWithAttrsVO;
        }).collect(Collectors.toList());

        return attrGroupWithAttrsVOS;
    }

    @Override
    public List<SkuItemVO.SpuItemAttrGroupVO> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId) {
        // 1、查出当前spu对应的所有属性分组信息以及当前分组下的所有属性对应的值
        return baseMapper.getAttrGroupWithAttrsBySpuId(spuId, catalogId);
    }
}