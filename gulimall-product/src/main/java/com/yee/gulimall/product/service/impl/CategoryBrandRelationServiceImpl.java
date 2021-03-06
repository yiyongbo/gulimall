package com.yee.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.yee.gulimall.product.dao.BrandDao;
import com.yee.gulimall.product.dao.CategoryDao;
import com.yee.gulimall.product.entity.BrandEntity;
import com.yee.gulimall.product.entity.CategoryEntity;
import com.yee.gulimall.product.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yee.common.utils.PageUtils;
import com.yee.common.utils.Query;

import com.yee.gulimall.product.dao.CategoryBrandRelationDao;
import com.yee.gulimall.product.entity.CategoryBrandRelationEntity;
import com.yee.gulimall.product.service.CategoryBrandRelationService;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Autowired
    BrandDao brandDao;

    @Autowired
    CategoryDao categoryDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();
        // 1、查询详细名字
        BrandEntity brandEntity = brandDao.selectById(brandId);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);

        categoryBrandRelation.setBrandName(brandEntity.getName());
        categoryBrandRelation.setCatelogName(categoryEntity.getName());

        this.save(categoryBrandRelation);
    }

    @Override
    public void updateBrand(Long brandId, String name) {
        CategoryBrandRelationEntity categoryBrandRelationEntity = new CategoryBrandRelationEntity();
        categoryBrandRelationEntity.setBrandId(brandId);
        categoryBrandRelationEntity.setBrandName(name);
        this.update(
                categoryBrandRelationEntity,
                new UpdateWrapper<CategoryBrandRelationEntity>().lambda().eq(CategoryBrandRelationEntity::getBrandId, brandId)
        );
    }

    @Override
    public void updateCategory(Long catId, String name) {
        this.baseMapper.updateCategory(catId, name);
    }

    @Override
    public List<BrandEntity> getBrandsByCatId(Long catId) {
        List<CategoryBrandRelationEntity> relationEntityList = this.list(
                new QueryWrapper<CategoryBrandRelationEntity>().lambda()
                        .eq(CategoryBrandRelationEntity::getCatelogId, catId)
        );
        List<Long> brandIds = relationEntityList.stream()
                .map(CategoryBrandRelationEntity::getBrandId).collect(Collectors.toList());

        return brandDao.selectBatchIds(brandIds);
    }

}