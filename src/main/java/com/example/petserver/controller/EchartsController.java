package com.example.petserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.petserver.entity.ChatMessage;
import com.example.petserver.entity.Orders;
import com.example.petserver.entity.PetInfo;
import com.example.petserver.entity.Product;
import com.example.petserver.entity.SysUser;
import com.example.petserver.service.IChatMessageService;
import com.example.petserver.service.IOrdersService;
import com.example.petserver.service.IPetInfoService;
import com.example.petserver.service.IProductService;
import com.example.petserver.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/echarts")
public class EchartsController {

	@Autowired
	private IOrdersService ordersService;

	@Autowired
	private IPetInfoService petInfoService;

	@Autowired
	private IProductService productService;

	@Autowired
	private ISysUserService sysUserService;

	@Autowired
	private IChatMessageService chatMessageService;

	// 1. [运营] 交易走势 (支持时间段筛选)
	@GetMapping("/revenueTrend")
	public Map<String, Object> revenueTrend(@RequestParam(required = false) String startTime, @RequestParam(required = false) String endTime) {
		Map<String, Object> map = new HashMap<>();
		List<String> dateList = getDatesBetween(startTime, endTime);
		List<BigDecimal> valueList = new ArrayList<>();

		// 优化：只查指定时间段内的订单
		QueryWrapper<Orders> query = new QueryWrapper<Orders>().ge("status", 1);
		if (startTime != null && !"".equals(startTime)) query.ge("create_time", startTime + " 00:00:00");
		if (endTime != null && !"".equals(endTime)) query.le("create_time", endTime + " 23:59:59");

		List<Orders> orders = ordersService.list(query);

		for (String dateStr : dateList) {
			BigDecimal dayTotal = orders.stream()
					.filter(o -> o.getCreateTime() != null && o.getCreateTime().toString().startsWith(dateStr))
					.map(Orders::getTotalAmount)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			valueList.add(dayTotal);
		}
		map.put("dates", dateList);
		map.put("values", valueList);
		return map;
	}

	// 2. [运营] 热门商品销量排行 (支持时间段筛选)
	@GetMapping("/salesRank")
	public Map<String, Object> salesRank(@RequestParam(required = false) String startTime, @RequestParam(required = false) String endTime) {
		QueryWrapper<Orders> query = new QueryWrapper<Orders>().ge("status", 1);
		if (startTime != null && !"".equals(startTime)) query.ge("create_time", startTime + " 00:00:00");
		if (endTime != null && !"".equals(endTime)) query.le("create_time", endTime + " 23:59:59");

		List<Orders> orders = ordersService.list(query);
		Map<Long, Integer> countMap = new HashMap<>();
		for (Orders order : orders) {
			countMap.put(order.getProductId(), countMap.getOrDefault(order.getProductId(), 0) + order.getQuantity());
		}

		Map<Long, String> nameMap = new HashMap<>();
		if (!countMap.isEmpty()) {
			List<Product> products = productService.listByIds(new ArrayList<>(countMap.keySet()));
			for (Product p : products) nameMap.put(p.getId(), p.getName());
		}

		List<Map.Entry<Long, Integer>> list = new ArrayList<>(countMap.entrySet());
		list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

		List<String> names = new ArrayList<>();
		List<Integer> values = new ArrayList<>();
		for (int i = 0; i < Math.min(5, list.size()); i++) {
			Long pid = list.get(i).getKey();
			names.add(nameMap.getOrDefault(pid, "已下架"));
			values.add(list.get(i).getValue());
		}
		Map<String, Object> res = new HashMap<>();
		res.put("names", names);
		res.put("values", values);
		return res;
	}

	// 3. [运营] 平台增长与活跃趋势 (新增)
	@GetMapping("/platformActivity")
	public Map<String, Object> platformActivity(@RequestParam(required = false) String startTime, @RequestParam(required = false) String endTime) {
		Map<String, Object> map = new HashMap<>();
		List<String> dateList = getDatesBetween(startTime, endTime);

		// 查询用户增长
		QueryWrapper<SysUser> userQuery = new QueryWrapper<>();
		if (startTime != null && !"".equals(startTime)) userQuery.ge("create_time", startTime + " 00:00:00");
		if (endTime != null && !"".equals(endTime)) userQuery.le("create_time", endTime + " 23:59:59");
		List<SysUser> users = sysUserService.list(userQuery);

		// 查询聊天活跃度
		QueryWrapper<ChatMessage> chatQuery = new QueryWrapper<>();
		if (startTime != null && !"".equals(startTime)) chatQuery.ge("create_time", startTime + " 00:00:00");
		if (endTime != null && !"".equals(endTime)) chatQuery.le("create_time", endTime + " 23:59:59");
		List<ChatMessage> messages = chatMessageService.list(chatQuery);

		List<Long> userCounts = new ArrayList<>();
		List<Long> chatCounts = new ArrayList<>();

		for (String dateStr : dateList) {
			long uCount = users.stream().filter(u -> u.getCreateTime() != null && u.getCreateTime().toString().startsWith(dateStr)).count();
			long cCount = messages.stream().filter(m -> m.getCreateTime() != null && m.getCreateTime().toString().startsWith(dateStr)).count();
			userCounts.add(uCount);
			chatCounts.add(cCount);
		}

		map.put("dates", dateList);
		map.put("userValues", userCounts);
		map.put("chatValues", chatCounts);
		return map;
	}

	// 4. [健康] 宠物年龄结构分布 (保持快照数据)
	@GetMapping("/petAgeDist")
	public List<Map<String, Object>> petAgeDist() {
		List<PetInfo> allPets = petInfoService.list();
		int young = 0, adult = 0, senior = 0;
		for (PetInfo pet : allPets) {
			if (pet.getAge() != null) {
				if (pet.getAge() <= 2) young++;
				else if (pet.getAge() <= 8) adult++;
				else senior++;
			}
		}
		List<Map<String, Object>> list = new ArrayList<>();
		list.add(createMap("幼年期 (0-2岁)", young));
		list.add(createMap("成年期 (3-8岁)", adult));
		list.add(createMap("老年期 (>9岁)", senior));
		return list;
	}

	// 辅助方法：生成日期列表
	private List<String> getDatesBetween(String start, String end) {
		List<String> dates = new ArrayList<>();
		LocalDate startDate;
		LocalDate endDate;

		if (start == null || "".equals(start)) {
			startDate = LocalDate.now().minusDays(6);
		} else {
			startDate = LocalDate.parse(start);
		}

		if (end == null || "".equals(end)) {
			endDate = LocalDate.now();
		} else {
			endDate = LocalDate.parse(end);
		}

		while (!startDate.isAfter(endDate)) {
			dates.add(startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
			startDate = startDate.plusDays(1);
		}
		return dates;
	}

	private Map<String, Object> createMap(String name, int value) {
		Map<String, Object> map = new HashMap<>();
		map.put("name", name);
		map.put("value", value);
		return map;
	}
}