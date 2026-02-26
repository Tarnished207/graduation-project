package com.example.petserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.petserver.entity.SysUser;
import com.example.petserver.mapper.SysUserMapper;
import com.example.petserver.service.ISysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author TAO
 * @since 2025-12-23
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

	@Override
	public boolean register(SysUser user) {
		// 1. 检查账号是否已存在
		QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("username", user.getUsername());
		long count = this.count(queryWrapper);
		if (count > 0) {
			return false; // 账号已存在，拒绝注册
		}

		// 2. 设置默认信息
		if (user.getRole() == null) {
			user.setRole("USER"); // 默认为普通用户
		}
		if (user.getNickname() == null) {
			user.setNickname("新用户" + System.currentTimeMillis());
		}
		if (user.getStatus() == null) {
			user.setStatus(1);
		}

		// 3. 保存到数据库
		return this.save(user);
	}

	@Override
	public SysUser login(String username, String password) {
		QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("username", username);
		queryWrapper.eq("password", password);
		// getOne 会去数据库查，如果查到唯一的记录就返回，查不到就返回 null
		return this.getOne(queryWrapper);
	}

}



