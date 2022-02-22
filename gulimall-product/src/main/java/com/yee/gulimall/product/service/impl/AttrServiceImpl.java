package com.yee.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.yee.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.yee.gulimall.product.dao.AttrGroupDao;
import com.yee.gulimall.product.dao.CategoryDao;
import com.yee.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.yee.gulimall.product.entity.AttrGroupEntity;
import com.yee.gulimall.product.entity.CategoryEntity;
import com.yee.gulimall.product.service.CategoryService;
import com.yee.gulimall.product.vo.AttrResponseVO;
import com.yee.gulimall.product.vo.AttrVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
        attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
        attrAttrgroupRelationEntity.setAttrGroupId(vo.getAttrGroupId());
        attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);

    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId) {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<>();
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
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(
                    new QueryWrapper<AttrAttrgroupRelationEntity>().lambda()
                            .eq(AttrAttrgroupRelationEntity::getAttrId, attrEntity.getAttrId())
            );

            if (Objects.nonNull(attrAttrgroupRelationEntity)) {
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrAttrgroupRelationEntity.getAttrGroupId());
                attrResponseVO.setGroupName(attrGroupEntity.getAttrGroupName());
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