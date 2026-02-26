package com.example.petserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.petserver.entity.PetInfo;
import com.example.petserver.mapper.PetInfoMapper;
import com.example.petserver.service.IPetInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * <p>
 * 宠物档案表 服务实现类
 * </p>
 *
 * @author TAO
 * @since 2025-12-23
 */
@Service
public class PetInfoServiceImpl extends ServiceImpl<PetInfoMapper, PetInfo> implements IPetInfoService {
	@Override
	public List<PetInfo> getByUserId(Long userId) {
		//构造查询条件：select * from pet_info where user_id = ?
		QueryWrapper<PetInfo> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("user_id", userId);
		return this.list(queryWrapper);
	}
}
