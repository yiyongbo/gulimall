package com.yee.gulimall.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yee.common.utils.PageUtils;
import com.yee.common.utils.Query;
import com.yee.gulimall.member.dao.MemberDao;
import com.yee.gulimall.member.dao.MemberLevelDao;
import com.yee.gulimall.member.entity.MemberEntity;
import com.yee.gulimall.member.entity.MemberLevelEntity;
import com.yee.gulimall.member.exception.PhoneExistException;
import com.yee.gulimall.member.exception.UsernameExistException;
import com.yee.gulimall.member.service.MemberLevelService;
import com.yee.gulimall.member.service.MemberService;
import com.yee.gulimall.member.vo.UserLoginVO;
import com.yee.gulimall.member.vo.UserRegisterVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("MemberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelService memberLevelService;
    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(UserRegisterVO userRegisterVO) {
        MemberEntity memberEntity = new MemberEntity();
        // 设置默认等级
        MemberLevelEntity memberLevelEntity = memberLevelService.lambdaQuery().eq(MemberLevelEntity::getDefaultStatus, 1).one();
        memberEntity.setLevelId(memberLevelEntity.getId());
        // 检查用户名和手机号是否唯一。为了让controller能感知异常，异常机制
        checkUsernameUnique(userRegisterVO.getUsername());
        checkPhoneUnique(userRegisterVO.getPhone());

        memberEntity.setUsername(userRegisterVO.getUsername());
        memberEntity.setMobile(userRegisterVO.getPhone());
        // 密码进行加密存储
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        memberEntity.setPassword(bCryptPasswordEncoder.encode(userRegisterVO.getPassword()));
        // 保存用户
        baseMapper.insert(memberEntity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        Integer count = lambdaQuery().eq(MemberEntity::getMobile, phone).count();
        if (count > 0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUsernameUnique(String username) throws UsernameExistException {
        Integer count = lambdaQuery().eq(MemberEntity::getUsername, username).count();
        if (count > 0) {
            throw new UsernameExistException();
        }
    }

    @Override
    public MemberEntity login(UserLoginVO userLoginVO) {
        MemberEntity memberEntity = lambdaQuery()
                .eq(MemberEntity::getUsername, userLoginVO.getLoginAccount())
                .or()
                .eq(MemberEntity::getMobile, userLoginVO.getLoginAccount()).one();
        if (memberEntity == null) {
            // 登录失败
            return null;
        } else {
            // 1、获取到数据库的password
            String passwordDb = memberEntity.getPassword();
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            // 密码匹配
            boolean matches = bCryptPasswordEncoder.matches(userLoginVO.getPassword(), passwordDb);
            if (matches) {
                return memberEntity;
            } else {
                return null;
            }
        }
    }

}