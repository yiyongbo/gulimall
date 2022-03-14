package com.yee.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yee.common.utils.PageUtils;
import com.yee.common.utils.Query;
import com.yee.gulimall.product.dao.CategoryDao;
import com.yee.gulimall.product.entity.CategoryEntity;
import com.yee.gulimall.product.service.CategoryBrandRelationService;
import com.yee.gulimall.product.service.CategoryService;
import com.yee.gulimall.product.vo.Catalog2VO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1、查出所有分类
        List<CategoryEntity> list = baseMapper.selectList(null);
        //2、组装成父子的树形结构
        //2.1 找到所有的一级分类
        return list.stream()
                .filter(item -> item.getCatLevel() == 1)
                .peek(item -> item.setChildren(getChildren(item, list)))
                .sorted(Comparator.comparing(CategoryEntity::getSort))
                .collect(Collectors.toList());
    }

    @Override
    public void removeMenuByIds(List<Long> singletonList) {
        // TODO 检查当前删除的菜单，是否被别的地方引用
        // 逻辑删除
        baseMapper.deleteBatchIds(singletonList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);
        return paths.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新所有关联的数据
     *
     * @param category 分类信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCascade(CategoryEntity category) {
        CategoryEntity dbCategoryEntity = this.getById(category.getCatId());
        this.updateById(category);
        if (StringUtils.hasText(category.getName()) && !dbCategoryEntity.getName().equals(category.getName())) {
            // 同步更新其他关联表中的数据
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());

            // TODO 更新其他关联
        }
    }

    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        return this.list(
                Wrappers.lambdaQuery(CategoryEntity.class)
                        .eq(CategoryEntity::getParentCid, 0)
        );
    }

    @Override
    public Map<String, List<Catalog2VO>> getCatalogJson() {

        // 将数据库的多次查询变为一次
        List<CategoryEntity> selectList = this.baseMapper.selectList(null);


        // 1、查出所有一级分类
        List<CategoryEntity> level1Categorys = getCategoryEntities(selectList, 0L);
        // 2、封装数据
        Map<String, List<Catalog2VO>> collect = level1Categorys.stream().collect(Collectors.toMap(
                k -> k.getCatId().toString(),
                v -> {
                    // 1、每一个的一级分类，查到这个一级分类的二级分类
                    List<CategoryEntity> categoryEntities = getCategoryEntities(selectList, v.getCatId());
                    // 2、封装上面的结果
                    return categoryEntities.stream()
                            .map(l2 -> {
                                Catalog2VO catalog2VO = new Catalog2VO(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                                // 1、找当前二级分类的三级分类封装成vo
                                List<CategoryEntity> catalog3VOList = getCategoryEntities(selectList, l2.getCatId());
                                if (catalog3VOList != null) {
                                    // 2、封装成指定格式
                                    List<Catalog2VO.Catalog3VO> collect1 = catalog3VOList.stream()
                                            .map(l3 -> new Catalog2VO.Catalog3VO(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName()))
                                            .collect(Collectors.toList());
                                    catalog2VO.setCatalog3List(collect1);
                                }
                                return catalog2VO;
                            })
                            .collect(Collectors.toList());
                }
        ));
        return collect;
    }

    private List<CategoryEntity> getCategoryEntities(List<CategoryEntity> selectList, Long parentCid) {
        return selectList.stream().filter(item -> item.getParentCid().equals(parentCid)).collect(Collectors.toList());
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        // 1、收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;
    }

    /**
     * 递归查找所有菜单的子菜单
     *
     * @param root 当前菜单
     * @param all  所有菜单
     * @return List<CategoryEntity>
     */
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        return all.stream()
                .filter(item -> item.getParentCid().equals(root.getCatId()))
                .peek(item -> item.setChildren(getChildren(item, all)))
                .sorted(Comparator.comparing(CategoryEntity::getSort))
                .collect(Collectors.toList());
    }
}