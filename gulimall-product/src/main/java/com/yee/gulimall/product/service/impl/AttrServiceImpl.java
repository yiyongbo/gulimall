package com.yee.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yee.common.constant.ProductConstant;
import com.yee.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.yee.gulimall.product.dao.AttrGroupDao;
import com.yee.gulimall.product.dao.CategoryDao;
import com.yee.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.yee.gulimall.product.entity.AttrGroupEntity;
import com.yee.gulimall.product.entity.CategoryEntity;
import com.yee.gulimall.product.service.CategoryService;
import com.yee.gulimall.product.vo.AttrGroupRelationVO;
import com.yee.gulimall.product.vo.AttrResponseVO;
import com.yee.gulimall.product.vo.AttrVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yee.common.utils.PageUtils;
import com.yee.common.utils.Query;

import com.yee.gulimall.product.dao.AttrDao;
import com.yee.gulimall.product.entity.AttrEntity;
import com.yee.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveAttr(AttrVO vo) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(vo, attrEntity);
        // 1、保存基本数据
        this.save(attrEntity);
        // 2、保存关联关系
        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && vo.getAttrGroupId() != null) {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationEntity.setAttrGroupId(vo.getAttrGroupId());
            attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
        }

    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type) {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(AttrEntity::getAttrType, "base".equalsIgnoreCase(type) ?
                        ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        if (catelogId != 0) {
            queryWrapper.lambda().eq(AttrEntity::getCatelogId, catelogId);
        }
        String key = (String) params.get("key");
        if (StringUtils.hasText(key)) {
            queryWrapper.lambda().and((wrapper) -> {
                wrapper.eq(AttrEntity::getAttrId, key).or().like(AttrEntity::getAttrName, key);
            });
        }

        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params), queryWrapper
        );

        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> list = (List<AttrEntity>) pageUtils.getList();

        List<AttrResponseVO> responseVOList = list.stream().map((attrEntity) -> {
            AttrResponseVO attrResponseVO = new AttrResponseVO();
            BeanUtils.copyProperties(attrEntity, attrResponseVO);

            // 1、设置分类和分组的名字
            if ("base".equalsIgnoreCase(type)) {
                AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(
                        new QueryWrapper<AttrAttrgroupRelationEntity>().lambda()
                                .eq(AttrAttrgroupRelationEntity::getAttrId, attrEntity.getAttrId())
                );

                if (Objects.nonNull(attrAttrgroupRelationEntity) && attrAttrgroupRelationEntity.getAttrGroupId() != null) {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
                    attrResponseVO.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }

            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (Objects.nonNull(categoryEntity)) {
                attrResponseVO.setCatelogName(categoryEntity.getName());
            }
            return attrResponseVO;
        }).collect(Collectors.toList());

        pageUtils.setList(responseVOList);
        return pageUtils;
    }

    @Override
    public AttrResponseVO getAttrInfo(Long attrId) {
        AttrResponseVO attrResponseVO = new AttrResponseVO();
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, attrResponseVO);

        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            // 1、设置分组信息
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(
                    new QueryWrapper<AttrAttrgroupRelationEntity>().lambda()
                            .eq(AttrAttrgroupRelationEntity::getAttrId, attrEntity.getAttrId())
            );
            if (Objects.nonNull(attrAttrgroupRelationEntity)) {
                attrResponseVO.setAttrGroupId(attrAttrgroupRelationEntity.getAttrGroupId());
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
                if (Objects.nonNull(attrGroupEntity)) {
                    attrResponseVO.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }

        // 2、设置分类信息
        Long catelogId = attrEntity.getCatelogId();
        Long[] catelogPath = categoryService.findCatelogPath(catelogId);
        attrResponseVO.setCatelogPath(catelogPath);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        Optional.ofNullable(categoryEntity).ifPresent((item) -> {
            attrResponseVO.setCatelogName(item.getName());
        });

        return attrResponseVO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAttr(AttrVO vo) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(vo, attrEntity);
        this.updateById(attrEntity);

        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            // 1、修改分组关联
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            attrAttrgroupRelationEntity.setAttrGroupId(vo.getAttrGroupId());
            attrAttrgroupRelationEntity.setAttrId(vo.getAttrId());

            Integer count = attrAttrgroupRelationDao.selectCount(
                    new QueryWrapper<AttrAttrgroupRelationEntity>().lambda()
                            .eq(AttrAttrgroupRelationEntity::getAttrId, vo.getAttrId())
            );
            if (count > 0) {
                attrAttrgroupRelationDao.update(
                        attrAttrgroupRelationEntity,
                        new UpdateWrapper<AttrAttrgroupRelationEntity>().lambda()
                                .eq(AttrAttrgroupRelationEntity::getAttrId, vo.getAttrId())
                );
            } else {
                attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
            }
        }
    }

    /**
     * 根据分组id查找关联的所有基本属性
     * @param attrgroundId 分组id
     * @return 基本属性集合
     */
    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroundId) {
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = attrAttrgroupRelationDao.selectList(
                new QueryWrapper<AttrAttrgroupRelationEntity>().lambda()
                        .eq(AttrAttrgroupRelationEntity::getAttrGroupId, attrgroundId)
        );
        List<Long> attrIds = attrAttrgroupRelationEntities.stream()
                .map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        return attrIds.size() > 0 ? this.listByIds(attrIds) : null;
    }

    @Override
    public void deleteRelation(AttrGroupRelationVO[] vos) {
        List<AttrAttrgroupRelationEntity> entities = Arrays.stream(vos).map((item) -> {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, attrAttrgroupRelationEntity);
            return attrAttrgroupRelationEntity;
        }).collect(Collectors.toList());

        attrAttrgroupRelationDao.deleteBatchRelation(entities);
    }

    /**
     * 获取当前分组没有关联的所有属性
     * @param params
     * @param attrgroupId
     * @return
     */
    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {
        // 1、当前分组只能关联自己所属的分类里面的所有属性
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        // 2、当前分组只能关联别的分组没有引用的属性
        // 2.1、当前分类下的其他分组
        List<AttrGroupEntity> attrGroupEntities = attrGroupDao.selectList(
                new QueryWrapper<AttrGroupEntity>().lambda()
                        .eq(AttrGroupEntity::getCatelogId, catelogId)
        );
        List<Long> attrGroupIds = attrGroupEntities.stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());
        // 2.2、这些分组管理的属性
        List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = attrAttrgroupRelationDao.selectList(
                new QueryWrapper<AttrAttrgroupRelationEntity>().lambda().in(AttrAttrgroupRelationEntity::getAttrGroupId, attrGroupIds)
        );
        List<Long> attrIds = attrAttrgroupRelationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        // 2.3、从当前分类的所有属性中移除这些属性
        LambdaQueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().lambda()
                .eq(AttrEntity::getCatelogId, catelogId)
                .eq(AttrEntity::getAttrType, ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode())
                .notIn(attrIds.size() > 0,AttrEntity::getAttrId, attrIds);
        String key = (String) params.get("key");
        if (StringUtils.hasText(key)) {
            queryWrapper.and((item) -> {
               item.eq(AttrEntity::getAttrId, key).or().like(AttrEntity::getAttrName, key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper);
        return new PageUtils(page);
    }

}