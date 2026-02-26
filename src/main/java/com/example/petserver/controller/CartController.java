package com.example.petserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.petserver.entity.CartItem;
import com.example.petserver.service.ICartItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

	@Autowired
	private ICartItemService cartService;

	// ==========================================
	// 1. 加入购物车 (逻辑：存在则累加，不存在则新增)
	// ==========================================
	@PostMapping("/add")
	public boolean add(@RequestBody CartItem cartItem) {
		if (cartItem.getQuantity() == null || cartItem.getQuantity() <= 0) {
			cartItem.setQuantity(1);
		}

		// 查重：同一个用户、同一个商品
		QueryWrapper<CartItem> query = new QueryWrapper<>();
		query.eq("user_id", cartItem.getUserId());
		query.eq("product_id", cartItem.getProductId());

		CartItem existingItem = cartService.getOne(query, false);

		if (existingItem != null) {
			// 如果已存在 -> 累加数量 (原数量 + 新增数量)
			existingItem.setQuantity(existingItem.getQuantity() + cartItem.getQuantity());
			return cartService.updateById(existingItem);
		} else {
			// 如果不存在 -> 新增一条
			cartItem.setCreateTime(LocalDateTime.now());
			return cartService.save(cartItem);
		}
	}

	// ==========================================
	// 2. [新增] 修改商品数量 (逻辑：直接覆盖为新数量)
	// 用于购物车页面的 "+" "-" 按钮
	// ==========================================
	// URL: POST http://localhost:9090/cart/update
	@PostMapping("/update")
	public boolean update(@RequestBody CartItem cartItem) {
		// 这里的 cartItem 只需要传 id (购物车记录ID) 和 quantity (新数量)

		// 简单校验：数量不能小于 1
		if (cartItem.getQuantity() != null && cartItem.getQuantity() < 1) {
			return false;
		}

		// MyBatis-Plus 自动根据 ID 更新非空字段 (这里只更新 quantity)
		return cartService.updateById(cartItem);
	}

	// ==========================================
	// 3. 查看我的购物车
	// ==========================================
	@GetMapping("/list/{userId}")
	public List<CartItem> list(@PathVariable Long userId) {
		return cartService.getMyCart(userId);
	}

	// ==========================================
	// 4. 移除购物车某项
	// ==========================================
	@GetMapping("/delete/{id}")
	public boolean delete(@PathVariable Long id) {
		return cartService.removeById(id);
	}

	// ==========================================
	// 5. 购物车结算
	// ==========================================
	@PostMapping("/checkout")
	public boolean checkout(@RequestBody CheckoutRequest request) {
		return cartService.checkout(request.getCartIds(), request.getUserId(), request.getAddress());
	}

	// 辅助类
	public static class CheckoutRequest {
		private List<Long> cartIds;
		private Long userId;
		private String address;

		public List<Long> getCartIds() { return cartIds; }
		public void setCartIds(List<Long> cartIds) { this.cartIds = cartIds; }
		public Long getUserId() { return userId; }
		public void setUserId(Long userId) { this.userId = userId; }
		public String getAddress() { return address; }
		public void setAddress(String address) { this.address = address; }
	}
}