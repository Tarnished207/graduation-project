package com.example.petserver.service;

import com.example.petserver.entity.PetInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * <p>
 * 宠物档案表 服务类
 * </p>
 *
 * @author TAO
 * @since 2025-12-23
 */
public interface IPetInfoService extends IService<PetInfo> {
	//根据用户ID，查询他所有的宠物
	List<PetInfo> getByUserId(Long userId);
}
