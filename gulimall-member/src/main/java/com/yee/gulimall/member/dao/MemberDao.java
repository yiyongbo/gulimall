package com.yee.gulimall.member.dao;

import com.yee.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author yee
 * @email yyb990929@gmail.com
 * @date 2021-12-08 23:12:00
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
