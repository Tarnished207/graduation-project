package com.example.petserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.petserver.entity.Orders;
import com.example.petserver.entity.Product;
import com.example.petserver.entity.SysUser; // 引入用户实体
import com.example.petserver.service.IOrdersService;
import com.example.petserver.service.IProductService;
import com.example.petserver.service.ISysUserService; // 引入用户服务
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import com.example.petserver.annotation.RequireRole;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrdersController {

	@Autowired
	private IOrdersService ordersService;

	@Autowired
	private IProductService productService;

	@Autowired
	private ISysUserService sysUserService; // [新增] 用来查买家是谁

	// 1. 下单接口
	@PostMapping("/add")
	public boolean add(@RequestBody Orders orders) {
		Product product = productService.getById(orders.getProductId());
		if (product == null || product.getStock() < orders.getQuantity()) {
			return false;
		}
		product.setStock(product.getStock() - orders.getQuantity());
		productService.updateById(product);

		orders.setCreateTime(LocalDateTime.now());
		orders.setStatus(0); // 默认待支付
		orders.setTotalAmount(product.getPrice().multiply(new BigDecimal(orders.getQuantity())));
		orders.setOrderNo(UUID.randomUUID().toString().replace("-", ""));
		return ordersService.save(orders);
	}


	// 2. 列表查询
	@GetMapping("/list")
	public List<Orders> list(
			@RequestParam(defaultValue = "") String orderNo,
			@RequestParam(required = false) Integer status,
			@RequestParam(required = false) Long userId
	) {
		QueryWrapper<Orders> query = new QueryWrapper<>();
		if (!"".equals(orderNo)) query.like("order_no", orderNo);
		if (status != null) query.eq("status", status);
		if (userId != null) query.eq("user_id", userId);
		query.orderByDesc("create_time");

		List<Orders> list = ordersService.list(query);

		for (Orders order : list) {
			// 1. 填商品
			Product p = productService.getById(order.getProductId());
			if (p != null) {
				order.setProductName(p.getName());
				order.setProductImage(p.getImage());
				order.setProductDescription(p.getDescription()); // 补充描述
			}

			// 2. 填买家
			if (order.getUserId() != null) {
				SysUser user = sysUserService.getById(order.getUserId());
				if (user != null) {
					order.setBuyerName(user.getNickname() == null ? user.getUsername() : user.getNickname());
					order.setBuyerPhone(user.getPhone());
				} else {
					order.setBuyerName("未知用户");
				}
			}
		}
		return list;
	}

	// 3. [新增] 管理员强制修改订单 (改状态、改地址)
// 3. [修改后] 管理员/用户修改订单 (包含取消订单自动回滚库存逻辑)
	@PostMapping("/update")
	public boolean update(@RequestBody Orders orders, HttpServletRequest request) {
		// 1. 先查出数据库里原始的订单信息 (为了拿到原来的状态、商品ID、购买数量)
		Orders oldOrder = ordersService.getById(orders.getId());
		if (oldOrder == null) {
			return false; // 订单不存在
		}

		// 权限校验：客服(SERVICE)不允许修改订单状态
		String role = (String) request.getAttribute("currentUserRole");
		if ("SERVICE".equals(role)) {
			// 如果客服尝试修改状态，强制恢复为旧状态
			if (orders.getStatus() != null && !orders.getStatus().equals(oldOrder.getStatus())) {
				orders.setStatus(oldOrder.getStatus());
			}
		}

		// 2. 【核心逻辑】检查是否是“取消订单”操作
		// 条件：前端传来的新状态是 -1 (已取消)，但数据库里的旧状态还不是 -1
		if (orders.getStatus() != null && orders.getStatus() == -1 && oldOrder.getStatus() != -1) {
			// 查出对应的商品
			Product product = productService.getById(oldOrder.getProductId());
			if (product != null) {
				// 把库存加回去 (原库存 + 订单数量)
				product.setStock(product.getStock() + oldOrder.getQuantity());
				productService.updateById(product);
				System.out.println("订单取消，库存已回滚: " + product.getName() + " +" + oldOrder.getQuantity());
			}
		}

		// 3. 最后再更新订单状态
		return ordersService.updateById(orders);
	}

	// 4. 发货 (仅限管理员)
	@PostMapping("/ship")
	@RequireRole("ADMIN")
	public boolean ship(@RequestBody Orders order) {
		Orders old = ordersService.getById(order.getId());
		if (old != null) {
			old.setStatus(2); // 变成已发货
			return ordersService.updateById(old);
		}
		return false;
	}

	// 5. 删除
	@GetMapping("/delete/{id}")
	public boolean delete(@PathVariable Long id) {
		return ordersService.removeById(id);
	}

	// 6. 统计看板
	@GetMapping("/stats")
	public Map<String, Object> stats() {
		Map<String, Object> map = new HashMap<>();
		try {
			map.put("total", ordersService.count());
			map.put("toShip", ordersService.count(new QueryWrapper<Orders>().eq("status", 1)));
			map.put("pending", ordersService.count(new QueryWrapper<Orders>().eq("status", 0)));

			// 计算营收
			BigDecimal revenue = BigDecimal.ZERO;
			List<Orders> list = ordersService.list(new QueryWrapper<Orders>().ge("status", 1));
			for (Orders o : list) {
				if (o.getTotalAmount() != null) revenue = revenue.add(o.getTotalAmount());
			}
			map.put("revenue", revenue);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	// 7. [新增] 用户端个人统计看板
	@GetMapping("/my-stats/{userId}")
	public Map<String, Object> myStats(@PathVariable Long userId) {
		Map<String, Object> map = new HashMap<>();
		try {
			// 1. 累计消费 (只算已支付、已发货、已完成的)
			BigDecimal totalSpent = BigDecimal.ZERO;
			List<Orders> paidOrders = ordersService.list(new QueryWrapper<Orders>()
					.eq("user_id", userId)
					.in("status", 1, 2, 3)); // 1-待发货 2-已发货 3-已完成
			for (Orders o : paidOrders) {
				if (o.getTotalAmount() != null) totalSpent = totalSpent.add(o.getTotalAmount());
			}
			map.put("totalSpent", totalSpent);

			// 2. 累计订单数 (不含取消)
			map.put("orderCount", ordersService.count(new QueryWrapper<Orders>()
					.eq("user_id", userId)
					.ne("status", -1)));

			// 3. 待支付
			map.put("pendingPay", ordersService.count(new QueryWrapper<Orders>()
					.eq("user_id", userId)
					.eq("status", 0)));

			// 4. 待收货 (含待发货1 + 已发货2)
			map.put("processing", ordersService.count(new QueryWrapper<Orders>()
					.eq("user_id", userId)
					.in("status", 1, 2)));

		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

}