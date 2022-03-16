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
    // @Caching(evict = {
    //         @CacheEvict(value = "category", key = "'level1Category'"),
    //         @CacheEvict(value = "category", key = "'getCatalogJson'")
    // })
    // @CachePut 双写模式
    // @CacheEvict 失效模式
    @CacheEvict(value = "category", allEntries = true)
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

    /**
     * @Cacheable 代表当前方法的结果需要缓存。如果缓存中有，方法不用调用。如果缓存中没有，会调用方法，最后将方法的结果放入缓存。
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

    public Map<String, List<Catalog2VO>> getCatalogJson2() throws JsonProcessingException {

        // 1、空结果缓存：解决缓存穿透
        // 2、设置随机过期时间：解决缓存雪崩问题
        // 3、加锁：解决缓存击穿

        // 1、加入缓存逻辑
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.hasText(catalogJson)) {
            // 缓存中有，转为我们指定的对象
            return objectMapper.readValue(catalogJson, new TypeReference<Map<String, List<Catalog2VO>>>() {
            });
        } else {
            // 缓存中没有，查询数据库
            return getCatalogJsonFromDbWithRedissonLock();
        }
    }

    /**
     * 缓存里面的数据如何和数据库保持一致
     * 缓存数据一致性
     * 1）、双写模式
     * 2）、失效模式
     * @return
     * @throws JsonProcessingException
     */
    public Map<String, List<Catalog2VO>> getCatalogJsonFromDbWithRedissonLock() throws JsonProcessingException {

        // 1、占分布式锁。去redis占坑
        // 锁的名字，锁的粒度，越细越快
        // 锁的粒度：具体缓存的是某个数据，11号商品：product-11-lock
        RLock lock = redisson.getLock("catalogJson-lock");
        lock.lock();
        // 加锁成功,执行业务
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
        // 1、占分布式锁。去redis占坑
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300L, TimeUnit.SECONDS);
        if (lock) {
            // 加锁成功,执行业务
            Map<String, List<Catalog2VO>> dataFromDb;
            try {
                dataFromDb = getDataFromDb();
            } finally {
                // 删除自己的锁，必须原子操作（lua脚本解锁）
                String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                // 删除锁
                redisTemplate.execute(new DefaultRedisScript<>(luaScript, Long.class), Collections.singletonList("lock"), uuid);
            }
            return dataFromDb;
        } else {
            // 加锁失败，重试，自旋方式
            return getCatalogJsonFromDbWithRedisLock();
        }
    }

    private Map<String, List<Catalog2VO>> getDataFromDb() throws JsonProcessingException {
        String catalogJson = redisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.hasText(catalogJson)) {
            return objectMapper.readValue(catalogJson, new TypeReference<Map<String, List<Catalog2VO>>>() {
            });
        }

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
        // 查到的数据转为json放入缓存
        redisTemplate.opsForValue().set("catalogJson", objectMapper.writeValueAsString(collect), 1, TimeUnit.DAYS);
        return collect;
    }


    /**
     * 从数据库查询并封装分类数据
     */
    public Map<String, List<Catalog2VO>> getCatalogJsonFromDbWithLocalLock() throws JsonProcessingException {

        synchronized (this) {
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
            // 查到的数据转为json放入缓存
            redisTemplate.opsForValue().set("catalogJson", objectMapper.writeValueAsString(collect), 1, TimeUnit.DAYS);
            return collect;
        }
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