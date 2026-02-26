package com.example.petserver.component;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.petserver.entity.FeedPlan;
import com.example.petserver.service.IFeedPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component // 交给 Spring 管理
public class FeedTask {

	@Autowired
	private IFeedPlanService feedPlanService;

	/**
	 * 每周一凌晨 00:00:00 执行一次
	 * 逻辑：把本周所有已完成任务，状态重置为“未完成(0)”
	 */
	@Scheduled(cron = "0 0 0 ? * MON", zone = "Asia/Shanghai")
	public void resetWeeklyPlan() {
		System.out.println("⏰ 定时任务执行: 开始重置本周计划状态... " + LocalDateTime.now());

		UpdateWrapper<FeedPlan> updateWrapper = new UpdateWrapper<>();
		updateWrapper.set("status", 0);
		updateWrapper.eq("status", 1);

		boolean success = feedPlanService.update(updateWrapper);

		if (success) {
			System.out.println("✅ 本周计划已重置为[未完成]状态");
		}
	}
}
