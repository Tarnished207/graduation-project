package com.example.petserver.service;

import com.example.petserver.entity.HealthRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
/**
 * <p>
 * 健康记录表 服务类
 * </p>
 *
 * @author TAO
 * @since 2025-12-23
 */
public interface IHealthRecordService extends IService<HealthRecord> {
	// [新增] 根据宠物ID查询健康记录
	List<HealthRecord> getByPetId(Long petId);
}
