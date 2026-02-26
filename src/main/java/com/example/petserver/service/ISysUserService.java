package com.example.petserver.service;

import com.example.petserver.entity.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author TAO
 * @since 2025-12-23
 */
public interface ISysUserService extends IService<SysUser> {
	// 新增：注册方法，返回 true 表示成功，false 表示失败（比如账号已存在）
	boolean register(SysUser user);//注册
	SysUser login(String username, String password);//登录
}
