package com.yee.gulimall.product.dao;

import com.yee.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author yee
 * @email yyb990929@gmail.com
 * @date 2021-12-08 21:53:20
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
