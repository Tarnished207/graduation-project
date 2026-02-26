package com.example.petserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.petserver.entity.SysUser;
import com.example.petserver.service.ISysUserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.petserver.entity.PetInfo;
import com.example.petserver.service.IPetInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * <p>
 * 宠物档案表 前端控制器
 * </p>
 */
@RestController
@RequestMapping("/petInfo")
public class PetInfoController {

	@Autowired
	private ISysUserService sysUserService; // 用于查询主人名字

	@Autowired
	private IPetInfoService petInfoService;

	// 1. 添加宠物
	@PostMapping("/add")
	public boolean add(@RequestBody PetInfo petInfo) {
		return petInfoService.save(petInfo);
	}

	// 2. 修改宠物
	@PostMapping("/update")
	public boolean update(@RequestBody PetInfo petInfo) {
		return petInfoService.updateById(petInfo);
	}

	// 3. 删除宠物
	@GetMapping("/delete/{id}")
	public boolean delete(@PathVariable Long id) {
		return petInfoService.removeById(id);
	}

	// 4. [升级版] 查询所有宠物 (支持多维度搜索并填充主人名字)
	@GetMapping("/listAll")
	public List<PetInfo> listAll(
			@RequestParam(defaultValue = "") String name,
			@RequestParam(required = false) Integer type,
			@RequestParam(defaultValue = "") String breed,
			@RequestParam(required = false) Integer healthStatus,
			@RequestParam(required = false) Integer ageMin,
			@RequestParam(required = false) Integer ageMax,
			@RequestParam(defaultValue = "") String ownerName
	) {
		QueryWrapper<PetInfo> query = new QueryWrapper<>();

		// 1. 昵称模糊搜索
		if (!"".equals(name)) {
			query.like("nickname", name);
		}

		// 2. 类型搜索 (0-狗, 1-猫)
		if (type != null) {
			query.eq("type", type);
		}

		// 3. 品种模糊搜索
		if (!"".equals(breed)) {
			query.like("breed", breed);
		}

		// 4. 健康状态搜索
		if (healthStatus != null) {
			query.eq("health_status", healthStatus);
		}

		// 5. 年龄区间搜索
		if (ageMin != null) {
			query.ge("age", ageMin);
		}
		if (ageMax != null) {
			query.le("age", ageMax);
		}

		// 6. 按主人名字模糊搜索
		if (!"".equals(ownerName)) {
			// 先查出名字匹配的用户 ID
			QueryWrapper<SysUser> userQuery = new QueryWrapper<>();
			userQuery.and(w -> w.like("nickname", ownerName).or().like("username", ownerName));
			List<SysUser> users = sysUserService.list(userQuery);

			if (users.isEmpty()) {
				// 没找到匹配的主人，直接返回空列表
				return new java.util.ArrayList<>();
			}

			List<Long> userIds = users.stream().map(SysUser::getId).collect(java.util.stream.Collectors.toList());
			query.in("user_id", userIds);
		}

		// 按 ID 倒序排列，新添加的在前面
		query.orderByDesc("id");

		List<PetInfo> list = petInfoService.list(query);

		// [关键步骤] 遍历每个宠物，查询并设置主人名字 (OwnerName)
		for (PetInfo pet : list) {
			if (pet.getUserId() != null) {
				SysUser user = sysUserService.getById(pet.getUserId());
				if (user != null) {
					// 优先显示昵称，没有昵称显示用户名
					String showName = user.getNickname() != null ? user.getNickname() : user.getUsername();
					pet.setOwnerName(showName);
				} else {
					pet.setOwnerName("用户已注销");
				}
			}
		}

		return list;
	}


	// 5. [新增] 宠物统计数据 (用于前端仪表盘)
	@GetMapping("/stats")
	public Map<String, Object> stats() {
		Map<String, Object> map = new HashMap<>();

		// 总数
		map.put("total", petInfoService.count());

		// 狗狗数量 (type=0)
		map.put("dogCount", petInfoService.count(new QueryWrapper<PetInfo>().eq("type", 0)));

		// 猫猫数量 (type=1)
		map.put("catCount", petInfoService.count(new QueryWrapper<PetInfo>().eq("type", 1)));

		// 公宠数量 (sex=0)
		map.put("boyCount", petInfoService.count(new QueryWrapper<PetInfo>().eq("sex", 0)));

		// [计算真实平均年龄]
		List<PetInfo> allPets = petInfoService.list();
		double avg = allPets.stream()
				.filter(p -> p.getAge() != null) // 过滤掉没填年龄的
				.mapToInt(PetInfo::getAge)
				.average()
				.orElse(0.0);

		// 保留1位小数 (例如 2.5)
		map.put("avgAge", String.format("%.1f", avg));

		return map;
	}

	// 6. 查看某用户的宠物 (保留原有接口)
	@GetMapping("/list/{userId}")
	public List<PetInfo> list(@PathVariable Long userId) {
		return petInfoService.getByUserId(userId);
	}
}