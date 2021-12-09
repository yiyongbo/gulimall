package com.yee.gulimall.order.dao;

import com.yee.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author yee
 * @email yyb990929@gmail.com
 * @date 2021-12-08 23:12:45
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
