package com.example.petserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.petserver.common.Result;
import com.example.petserver.entity.SysNotice;
import com.example.petserver.mapper.SysNoticeMapper;
import com.example.petserver.service.ISysNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sysNotice")
public class SysNoticeController {

	@Autowired
	private SysNoticeMapper sysNoticeMapper;

	@Autowired
	private ISysNoticeService sysNoticeService;

	// 1. 获取当前用户的未读通知数量
	@GetMapping("/unreadCount")
	public Result getUnreadCount(@RequestParam Long userId) {
		QueryWrapper<SysNotice> query = new QueryWrapper<>();
		query.and(w -> w.isNull("user_id").or().eq("user_id", userId));
		query.and(w -> w.eq("is_read", 0).or().isNull("is_read"));
		return Result.success(sysNoticeMapper.selectCount(query));
	}

	// 2. 获取通知列表（带分页/限制）
	@GetMapping("/list")
	public Result getNoticeList(@RequestParam Long userId, @RequestParam(defaultValue = "5") Integer limit) {
		QueryWrapper<SysNotice> query = new QueryWrapper<>();
		query.and(w -> w.isNull("user_id").or().eq("user_id", userId));
		query.and(w -> w.eq("is_read", 0).or().isNull("is_read"));
		query.orderByDesc("create_time").last("LIMIT " + limit);
		return Result.success(sysNoticeMapper.selectList(query));
	}

	// 3. 一键全部已读
	@PostMapping("/readAll")
	public Result readAll(@RequestBody Map<String, Object> params) {
		Object userIdObj = params.get("userId");
		if (userIdObj == null) return Result.error("400", "参数缺失");

		Long userId = Long.valueOf(userIdObj.toString());

		UpdateWrapper<SysNotice> updateWrapper = new UpdateWrapper<>();
		updateWrapper.and(w -> w.isNull("user_id").or().eq("user_id", userId))
				.and(w -> w.eq("is_read", 0).or().isNull("is_read"))
				.set("is_read", 1);

		sysNoticeMapper.update(null, updateWrapper);
		return Result.success();
	}

	// 4. 批量发送通知（管理员使用）
	@PostMapping("/batchSend")
	public Result batchSend(@RequestBody Map<String, Object> params) {
		Object userIdsObj = params.get("userIds");
		if (!(userIdsObj instanceof List)) {
			return Result.error("400", "参数 userIds 格式错误");
		}

		List<?> rawList = (List<?>) userIdsObj;
		List<Long> userIds = rawList.stream()
				.map(obj -> Long.valueOf(obj.toString()))
				.collect(java.util.stream.Collectors.toList());

		String title = (String) params.get("title");
		String content = (String) params.get("content");

		boolean success = sysNoticeService.batchSend(userIds, title, content);
		return success ? Result.success() : Result.error("500", "发送失败");
	}
}
