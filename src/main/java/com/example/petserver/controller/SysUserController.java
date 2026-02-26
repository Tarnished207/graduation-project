package com.example.petserver.controller;

import com.example.petserver.entity.*;
import com.example.petserver.service.*;
import com.example.petserver.mapper.SysNoticeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.example.petserver.utils.JwtUtils; // 引入 JWT 工具类

@RestController
@RequestMapping("/sysUser")
public class SysUserController {

	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private IPetInfoService petInfoService;
	@Autowired
	private IOrdersService ordersService;
	@Autowired
	private IProductService productService;
	@Autowired
	private IHealthRecordService healthRecordService;
	@Autowired
	private SysNoticeMapper sysNoticeMapper;

	// 加 required=false 防止报错
	@Autowired(required = false)
	private JavaMailSender mailSender;

	// ==========================================
	// 1. 用户接口
	// ==========================================

	@PostMapping("/login")
	public Map<String, Object> login(@RequestBody SysUser user) {
		Map<String, Object> res = new HashMap<>();
		SysUser found = sysUserService.login(user.getUsername(), user.getPassword());
		if (found == null) {
			res.put("code", "400");
			res.put("msg", "账号或密钥错误");
			return res;
		}
		if (found.getStatus() != null && found.getStatus() == 0) {
			res.put("code", "403");
			res.put("msg", "账号已禁用");
			return res;
		}

		// --- 核心修改：生成 JWT Token ---
		String token = JwtUtils.createToken(found.getId(), found.getRole());
		
		res.put("code", "200");
		res.put("msg", "成功");
		res.put("data", found); // 用户信息
		res.put("token", token); // 返回 Token 给前端
		return res;
	}

	@GetMapping("/list")
	public List<SysUser> list(
			@RequestParam(defaultValue = "") String name,
			@RequestParam(defaultValue = "") String address,
			@RequestParam(required = false) String role
	) {
		QueryWrapper<SysUser> query = new QueryWrapper<>();
		if (!"".equals(name)) {
			query.and(q -> q.like("username", name).or().like("nickname", name).or().like("real_name", name));
		}
		if (!"".equals(address)) {
			query.like("address", address);
		}
		if (role != null && !"".equals(role)) {
			query.eq("role", role);
		}
		query.orderByDesc("id");
		return sysUserService.list(query);
	}

	@PostMapping("/register")
	public String register(@RequestBody SysUser user) {
		boolean success = sysUserService.register(user);
		return success ? "注册成功" : "注册失败：账号已存在";
	}

	@PostMapping("/update")
	public boolean update(@RequestBody SysUser user) {
		return sysUserService.updateById(user);
	}

	@GetMapping("/delete/{id}")
	public boolean delete(@PathVariable Long id) {
		return sysUserService.removeById(id);
	}

	// ==========================================
	// 2. 统计接口
	// ==========================================
	@GetMapping("/stats")
	public Map<String, Object> stats() {
		Map<String, Object> map = new HashMap<>();
		map.put("userCount", sysUserService.count());
		map.put("petCount", petInfoService.count());
		map.put("orderCount", ordersService.count());
		map.put("adminCount", sysUserService.count(new QueryWrapper<SysUser>().eq("role", "ADMIN")));

		BigDecimal total = BigDecimal.ZERO;
		List<Orders> allOrders = ordersService.list();
		for (Orders o : allOrders) {
			if (o.getTotalAmount() != null) total = total.add(o.getTotalAmount());
		}
		map.put("totalSales", total);
		return map;
	}

	// ==========================================
	// 3. 图表接口
	// ==========================================

	@GetMapping("/chart/revenue")
	public Map<String, Object> revenueChart() {
		List<String> dates = new ArrayList<>();
		List<BigDecimal> values = new ArrayList<>();
		List<Orders> allOrders = ordersService.list();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
		Map<String, BigDecimal> dailyMap = new HashMap<>();

		for (Orders order : allOrders) {
			if (order.getCreateTime() != null) {
				String dateStr = order.getCreateTime().format(formatter);
				BigDecimal amount = order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount();
				if (dailyMap.containsKey(dateStr)) dailyMap.put(dateStr, dailyMap.get(dateStr).add(amount));
				else dailyMap.put(dateStr, amount);
			}
		}
		dailyMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
			dates.add(entry.getKey());
			values.add(entry.getValue());
		});
		Map<String, Object> result = new HashMap<>();
		result.put("labels", dates);
		result.put("data", values);
		return result;
	}

	@GetMapping("/chart/productRank")
	public Map<String, Object> productRankChart() {
		Map<Long, Integer> countMap = new HashMap<>();
		List<Orders> allOrders = ordersService.list();
		for (Orders order : allOrders) {
			Long pid = order.getProductId();
			Integer qty = order.getQuantity() == null ? 0 : order.getQuantity();
			countMap.put(pid, countMap.getOrDefault(pid, 0) + qty);
		}
		List<String> names = new ArrayList<>();
		List<Integer> values = new ArrayList<>();
		countMap.forEach((pid, count) -> {
			Product p = productService.getById(pid);
			if (p != null) {
				names.add(p.getName());
				values.add(count);
			}
		});
		Map<String, Object> result = new HashMap<>();
		result.put("labels", names);
		result.put("data", values);
		return result;
	}

	// 【⭐核心修复点】 这里改成 getType()，与上面的实体类保持一致
	@GetMapping("/chart/health")
	public Map<String, Object> healthChart() {
		Map<String, Integer> typeMap = new HashMap<>();
		List<HealthRecord> records = healthRecordService.list();

		for (HealthRecord r : records) {
			// 修正：调用 getType() 而不是 getRecordType()
			String type = r.getType();
			if (type != null) {
				typeMap.put(type, typeMap.getOrDefault(type, 0) + 1);
			}
		}

		List<String> labels = new ArrayList<>();
		List<Integer> data = new ArrayList<>();
		typeMap.forEach((k, v) -> {
			labels.add(k);
			data.add(v);
		});
		Map<String, Object> result = new HashMap<>();
		result.put("labels", labels);
		result.put("data", data);
		return result;
	}

	// ==========================================
	// 4. 其他接口 (邮件、重置密码等)
	// ==========================================
	@GetMapping("/homeData")
	public Map<String, Object> homeData(@RequestParam(required = false) Long userId) {
		Map<String, Object> result = new HashMap<>();
		result.put("waitShipCount", ordersService.count(new QueryWrapper<Orders>().eq("status", 1)));
		result.put("lowStockCount", productService.count(new QueryWrapper<Product>().lt("stock", 10)));
		result.put("waitPayCount", ordersService.count(new QueryWrapper<Orders>().eq("status", 0)));
		return result;
	}

	private static Map<String, String> codeCache = new HashMap<>();

	@PostMapping("/sendEmail")
	public boolean sendEmail(@RequestBody Map<String, String> params) {
		String email = params.get("email");
		if (email == null || "".equals(email)) return false;
		String code = String.valueOf(new Random().nextInt(899999) + 100000);
		codeCache.put(email, code);
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom("3080828852@qq.com");
			message.setTo(email);
			message.setSubject("【爪爪星球】重置密码验证码");
			message.setText("您的验证码是：" + code);
			if (mailSender != null) mailSender.send(message);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@PostMapping("/resetPass")
	public boolean resetPass(@RequestBody Map<String, String> params) {
		String email = params.get("email");
		String code = params.get("code");
		String newPassword = params.get("newPassword");
		if (email == null || code == null || newPassword == null) return false;
		String correctCode = codeCache.get(email);
		if (correctCode == null || !correctCode.equals(code)) return false;

		UpdateWrapper<SysUser> updateWrapper = new UpdateWrapper<>();
		updateWrapper.eq("email", email).set("password", newPassword);
		boolean success = sysUserService.update(updateWrapper);
		if (!success) {
			UpdateWrapper<SysUser> uw2 = new UpdateWrapper<>();
			uw2.eq("username", email).set("password", newPassword);
			success = sysUserService.update(uw2);
		}
		if (success) codeCache.remove(email);
		return success;
	}

	@GetMapping("/username/{username}")
	public SysUser findByUsername(@PathVariable String username) {
		QueryWrapper<SysUser> q = new QueryWrapper<>();
		q.eq("username", username);
		return sysUserService.getOne(q);
	}

	@PostMapping("/password")
	public Map<String, Object> updatePassword(@RequestBody Map<String, String> params) {
		String username = params.get("username");
		String password = params.get("password");
		String newPassword = params.get("newPassword");
		Map<String, Object> res = new HashMap<>();
		QueryWrapper<SysUser> query = new QueryWrapper<>();
		query.eq("username", username);
		query.eq("password", password);
		SysUser user = sysUserService.getOne(query);
		if (user != null) {
			user.setPassword(newPassword);
			sysUserService.updateById(user);
			res.put("code", "200");
			res.put("msg", "成功");
			res.put("data", true);
			return res;
		} else {
			res.put("code", "400");
			res.put("msg", "旧密码错误");
			return res;
		}
	}
}
