package com.example.petserver.service.impl;

import com.example.petserver.entity.HealthRecord;
import com.example.petserver.mapper.HealthRecordMapper;
import com.example.petserver.service.IHealthRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;

/**
 * <p>
 * 健康记录表 服务实现类
 * </p>
 */
@Service
public class HealthRecordServiceImpl extends ServiceImpl<HealthRecordMapper, HealthRecord> implements IHealthRecordService {
	@Override
	public List<HealthRecord> getByPetId(Long petId) {
		QueryWrapper<HealthRecord> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("pet_id", petId);
		// 修复：改成 create_time (对应实体类的 createTime)
		queryWrapper.orderByDesc("create_time");
		return this.list(queryWrapper);
	}
}