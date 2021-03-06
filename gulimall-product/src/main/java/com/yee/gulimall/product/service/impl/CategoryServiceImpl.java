package com.yee.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yee.common.utils.PageUtils;
import com.yee.common.utils.Query;
import com.yee.gulimall.product.dao.CategoryDao;
import com.yee.gulimall.product.entity.CategoryEntity;
import com.yee.gulimall.product.service.CategoryBrandRelationService;
import com.yee.gulimall.product.service.CategoryService;
import com.yee.gulimall.product.vo.Catalog2VO;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    RedissonClient redisson;

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
        //1?????????????????????
        List<CategoryEntity> list = baseMapper.selectList(null);
        //2?????????????????????????????????
        //2.1 ???????????????????????????
        return list.stream()
                .filter(item -> item.getCatLevel() == 1)
                .peek(item -> item.setChildren(getChildren(item, list)))
                .sorted(Comparator.comparing(CategoryEntity::getSort))
                .collect(Collectors.toList());
    }

    @Override
    public void removeMenuByIds(List<Long> singletonList) {
        // TODO ?????????????????????????????????????????????????????????
        // ????????????
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
     * ?????????????????????????????????
     *
     * @param category ????????????
     */
    // @Caching(evict = {
    //         @CacheEvict(value = "category", key = "'level1Category'"),
    //         @CacheEvict(value = "category", key = "'getCatalogJson'")
    // })
    // @CachePut ????????????
    // @CacheEvict ????????????
    @CacheEvict(value = "category", allEntries = true)
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCascade(CategoryEntity category) {
        CategoryEntity dbCategoryEntity = this.getById(category.getCatId());
        this.updateById(category);
        if (StringUtils.hasText(category.getName()) && !dbCategoryEntity.getName().equals(category.getName())) {
            // ???????????????????????????????????????
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());

            // TODO ??????????????????
        }
    }

    /**
     * @Cacheable ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     */
    @Cacheable(value = "category", key = "'level1Category'")
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        return this.list(
                Wrappers.lambdaQuery(CategoryEntity.class)
                        .eq(CategoryEntity::getParentCid, 0)
        );
    }

    @Cacheable(value = "category", key = "#root.methodName")
    @Override
    public Map<String, List<Catalog2VO>> getCatalogJson() {
        // ???????????????????????????????????????
        List<CategoryEntity> selectList = this.baseMapper.selectList(null);

        // 1???????????????????????????
        List<CategoryEntity> level1Categorys = getCategoryEntities(selectList, 0L);
        // 2???????????????
        Map<String, List<Catalog2VO>> collect = level1Categorys.stream().collect(Collectors.toMap(
                k -> k.getCatId().toString(),
                v -> {
                    // 1?????????????????????????????????????????????????????????????????????
                    List<CategoryEntity> categoryEntities = getCategoryEntities(selectList, v.getCatId());
                    // 2????????????????????????
                    return categoryEntities.stream()
                            .map(l2 -> {
                                Catalog2VO catalog2VO = new Catalog2VO(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                                // 1????????????????????????????????????????????????vo
                                List<CategoryEntity> catalog3VOList = getCategoryEntities(selectList, l2.getCatId());
                                if (catalog3VOList != null) {
                                    // 2????????????????????????
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

    public Map<String, List<Catalog2VO>> getCatalogJson2() throws JsonProcessingException {

        // 1???????????????????????????????????????
        // 2??????????????????????????????????????????????????????
        // 3??????????????????????????????

        // 1?????????????????????
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.hasText(catalogJson)) {
            // ??????????????????????????????????????????
            return objectMapper.readValue(catalogJson, new TypeReference<Map<String, List<Catalog2VO>>>() {
            });
        } else {
            // ?????????????????????????????????
            return getCatalogJsonFromDbWithRedissonLock();
        }
    }

    /**
     * ???????????????????????????????????????????????????
     * ?????????????????????
     * 1??????????????????
     * 2??????????????????
     * @return
     * @throws JsonProcessingException
     */
    public Map<String, List<Catalog2VO>> getCatalogJsonFromDbWithRedissonLock() throws JsonProcessingException {

        // 1????????????????????????redis??????
        // ??????????????????????????????????????????
        // ????????????????????????????????????????????????11????????????product-11-lock
        RLock lock = redisson.getLock("catalogJson-lock");
        lock.lock();
        // ????????????,????????????
        Map<String, List<Catalog2VO>> dataFromDb;
        try {
            dataFromDb = getDataFromDb();
        } finally {
            lock.unlock();
        }
        return dataFromDb;
    }

    public Map<String, List<Catalog2VO>> getCatalogJsonFromDbWithRedisLock() throws JsonProcessingException {
        String uuid = UUID.randomUUID().toString();
        // 1????????????????????????redis??????
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300L, TimeUnit.SECONDS);
        if (lock) {
            // ????????????,????????????
            Map<String, List<Catalog2VO>> dataFromDb;
            try {
                dataFromDb = getDataFromDb();
            } finally {
                // ??????????????????????????????????????????lua???????????????
                String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                // ?????????
                redisTemplate.execute(new DefaultRedisScript<>(luaScript, Long.class), Collections.singletonList("lock"), uuid);
            }
            return dataFromDb;
        } else {
            // ????????????????????????????????????
            return getCatalogJsonFromDbWithRedisLock();
        }
    }

    private Map<String, List<Catalog2VO>> getDataFromDb() throws JsonProcessingException {
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.hasText(catalogJson)) {
            return objectMapper.readValue(catalogJson, new TypeReference<Map<String, List<Catalog2VO>>>() {
            });
        }

        // ???????????????????????????????????????
        List<CategoryEntity> selectList = this.baseMapper.selectList(null);

        // 1???????????????????????????
        List<CategoryEntity> level1Categorys = getCategoryEntities(selectList, 0L);
        // 2???????????????
        Map<String, List<Catalog2VO>> collect = level1Categorys.stream().collect(Collectors.toMap(
                k -> k.getCatId().toString(),
                v -> {
                    // 1?????????????????????????????????????????????????????????????????????
                    List<CategoryEntity> categoryEntities = getCategoryEntities(selectList, v.getCatId());
                    // 2????????????????????????
                    return categoryEntities.stream()
                            .map(l2 -> {
                                Catalog2VO catalog2VO = new Catalog2VO(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                                // 1????????????????????????????????????????????????vo
                                List<CategoryEntity> catalog3VOList = getCategoryEntities(selectList, l2.getCatId());
                                if (catalog3VOList != null) {
                                    // 2????????????????????????
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
        // ?????????????????????json????????????
        redisTemplate.opsForValue().set("catalogJson", objectMapper.writeValueAsString(collect), 1, TimeUnit.DAYS);
        return collect;
    }


    /**
     * ???????????????????????????????????????
     */
    public Map<String, List<Catalog2VO>> getCatalogJsonFromDbWithLocalLock() throws JsonProcessingException {

        synchronized (this) {
            // ???????????????????????????????????????
            List<CategoryEntity> selectList = this.baseMapper.selectList(null);

            // 1???????????????????????????
            List<CategoryEntity> level1Categorys = getCategoryEntities(selectList, 0L);
            // 2???????????????
            Map<String, List<Catalog2VO>> collect = level1Categorys.stream().collect(Collectors.toMap(
                    k -> k.getCatId().toString(),
                    v -> {
                        // 1?????????????????????????????????????????????????????????????????????
                        List<CategoryEntity> categoryEntities = getCategoryEntities(selectList, v.getCatId());
                        // 2????????????????????????
                        return categoryEntities.stream()
                                .map(l2 -> {
                                    Catalog2VO catalog2VO = new Catalog2VO(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                                    // 1????????????????????????????????????????????????vo
                                    List<CategoryEntity> catalog3VOList = getCategoryEntities(selectList, l2.getCatId());
                                    if (catalog3VOList != null) {
                                        // 2????????????????????????
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
            // ?????????????????????json????????????
            redisTemplate.opsForValue().set("catalogJson", objectMapper.writeValueAsString(collect), 1, TimeUnit.DAYS);
            return collect;
        }
    }

    private List<CategoryEntity> getCategoryEntities(List<CategoryEntity> selectList, Long parentCid) {
        return selectList.stream().filter(item -> item.getParentCid().equals(parentCid)).collect(Collectors.toList());
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        // 1?????????????????????id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;
    }

    /**
     * ????????????????????????????????????
     *
     * @param root ????????????
     * @param all  ????????????
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