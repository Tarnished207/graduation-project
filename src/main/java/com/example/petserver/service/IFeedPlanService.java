package com.example.petserver.service;

import com.example.petserver.entity.FeedPlan;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * <p>
 * 喂养计划表 服务类
 * </p>
 *
 * @author TAO
 * @since 2025-12-23
 */
public interface IFeedPlanService extends IService<FeedPlan> {
	//根据宠物ID，查询它的喂食计划
	List<FeedPlan> getByPetId(Long petId);
}
