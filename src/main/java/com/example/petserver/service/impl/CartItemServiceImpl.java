package com.example.petserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petserver.entity.CartItem;
import com.example.petserver.entity.Orders;
import com.example.petserver.entity.Product;
import com.example.petserver.mapper.CartItemMapper;
import com.example.petserver.service.ICartItemService;
import com.example.petserver.service.IOrdersService;
import com.example.petserver.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CartItemServiceImpl extends ServiceImpl<CartItemMapper, CartItem> implements ICartItemService {

	@Autowired
	private IProductService productService;
	@Autowired
	private IOrdersService ordersService;

	@Override
	public List<CartItem> getMyCart(Long userId) {
		QueryWrapper<CartItem> query = new QueryWrapper<>();
		query.eq("user_id", userId).orderByDesc("create_time");
		List<CartItem> items = this.list(query);

		for (CartItem item : items) {
			Product product = productService.getById(item.getProductId());
			if (product != null) {
				item.setProductName(product.getName());
				item.setPrice(product.getPrice());
				item.setDescription(product.getDescription());
				item.setImage(product.getImage());
			}
		}
		return items;
	}

	@Override
	@Transactional(rollbackFor = Exception.class) // 开启事务：遇到任何报错（如库存不足）自动回滚
	public boolean checkout(List<Long> cartIds, Long userId, String address) {
		// 1. 根据前端传来的购物车ID列表，查出这些记录
		List<CartItem> items = this.listByIds(cartIds);
		if (items == null || items.isEmpty()) return false;

		// 2. 遍历，为每一个购物车项生成一个订单
		for (CartItem item : items) {
			Product product = productService.getById(item.getProductId());
			if (product == null) continue;

			// --- [核心修改] 库存检查与扣减 ---
			// 检查库存
			if (product.getStock() < item.getQuantity()) {
				throw new RuntimeException("商品 [" + product.getName() + "] 库存不足，仅剩 " + product.getStock() + " 件");
			}

			// 扣减库存
			product.setStock(product.getStock() - item.getQuantity());
			productService.updateById(product); // 更新数据库中的商品库存
			// -----------------------------

			Orders order = new Orders();
			order.setUserId(userId);
			order.setProductId(item.getProductId());
			order.setQuantity(item.getQuantity());
			// 计算总价：单价 * 数量
			order.setTotalAmount(product.getPrice().multiply(new BigDecimal(item.getQuantity())));
			order.setAddress(address);
			order.setStatus(0); // 0 代表待支付
			order.setCreateTime(LocalDateTime.now());
			// 生成唯一订单号
			order.setOrderNo(UUID.randomUUID().toString().replace("-", ""));

			// 保存订单
			ordersService.save(order);
		}

		// 3. 生成完订单后，把购物车里的这些删掉
		this.removeByIds(cartIds);
		return true;
	}
}