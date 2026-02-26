package com.example.petserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.petserver.entity.FeedPlan;
import com.example.petserver.service.IFeedPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate; // 引入 LocalDate
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/feedPlan")
public class FeedPlanController {

	@Autowired
	private IFeedPlanService feedPlanService;

	// 1. 新增 (保持不变：自动分裂逻辑)
	@PostMapping("/add")
	public boolean add(@RequestBody FeedPlan feedPlan) {
		// 如果用户选了"每天" (weekDay=0 或 null)，则分裂成 7 条独立记录
		if (feedPlan.getWeekDay() == null || feedPlan.getWeekDay() == 0) {
			List<FeedPlan> planList = new ArrayList<>();
			for (int i = 1; i <= 7; i++) {
				FeedPlan plan = new FeedPlan();
				plan.setUserId(feedPlan.getUserId());
				plan.setPetId(feedPlan.getPetId());
				plan.setPlanName(feedPlan.getPlanName());
				plan.setPlanTime(feedPlan.getPlanTime());
				plan.setFoodName(feedPlan.getFoodName());
				plan.setAmount(feedPlan.getAmount());
				plan.setStatus(0); // 默认为未完成
				plan.setWeekDay(i); // 设置为具体的周几 (1-7)
				planList.add(plan);
			}
			return feedPlanService.saveBatch(planList);
		} else {
			// 如果是特定周几，直接保存一条
			return feedPlanService.save(feedPlan);
		}
	}

	// 2. 更新状态接口 (保持不变：记得更新时间)
	@PostMapping("/update")
	public boolean update(@RequestBody FeedPlan feedPlan) {
		// 当用户点击打勾(status=1)或取消(status=0)时，都更新一下时间
		feedPlan.setUpdateTime(LocalDateTime.now());
		return feedPlanService.updateById(feedPlan);
	}

	// 3. 删除
	@GetMapping("/delete/{id}")
	public boolean delete(@PathVariable Long id) {
		return feedPlanService.removeById(id);
	}

	// 4. 【全量净化版】按用户查询
	@GetMapping("/list/user/{userId}")
	public List<FeedPlan> listByUserId(@PathVariable Long userId) {
		QueryWrapper<FeedPlan> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("user_id", userId);
		List<FeedPlan> list = feedPlanService.list(queryWrapper);

		ZoneId zoneId = ZoneId.of("Asia/Shanghai");
		WeekFields weekFields = WeekFields.of(Locale.CHINA);
		LocalDate today = LocalDate.now(zoneId);
		int currentWeek = today.get(weekFields.weekOfWeekBasedYear());
		int currentWeekYear = today.get(weekFields.weekBasedYear());

		for (FeedPlan plan : list) {
			if (plan.getStatus() != null && plan.getStatus() == 1) {
				LocalDateTime updateTime = plan.getUpdateTime();
				boolean doneThisWeek = false;
				if (updateTime != null) {
					LocalDate doneDate = updateTime.toLocalDate();
					int doneWeek = doneDate.get(weekFields.weekOfWeekBasedYear());
					int doneWeekYear = doneDate.get(weekFields.weekBasedYear());
					doneThisWeek = doneWeek == currentWeek && doneWeekYear == currentWeekYear;
				}

				if (!doneThisWeek) {
					plan.setStatus(0);
					feedPlanService.updateById(plan);
				}
			}
		}

		return list;
	}

	// （每周重置计划状态）临时测试接口 (保留，用于手动测试)
	@GetMapping("/test/reset")
	public String testReset() {
		com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<FeedPlan> wrapper = new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
		wrapper.set("status", 0).eq("status", 1);
		boolean result = feedPlanService.update(wrapper);
		return "模拟新的一周成功！已重置本周所有计划状态 (结果:" + result + ")";
	}
}
