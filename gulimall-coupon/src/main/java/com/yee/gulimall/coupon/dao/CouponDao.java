package com.yee.gulimall.coupon.dao;

import com.yee.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author yee
 * @email yyb990929@gmail.com
 * @date 2021-12-08 23:07:37
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
