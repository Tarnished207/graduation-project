package com.example.petserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.petserver.entity.FeedPlan;
import com.example.petserver.mapper.FeedPlanMapper;
import com.example.petserver.service.IFeedPlanService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FeedPlanServiceImpl extends ServiceImpl<FeedPlanMapper, FeedPlan> implements IFeedPlanService {

	@Override
	public List<FeedPlan> getByPetId(Long petId) {
		// SQL: select * from feed_plan where pet_id = ?
		QueryWrapper<FeedPlan> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("pet_id", petId);
		return this.list(queryWrapper);
	}
}