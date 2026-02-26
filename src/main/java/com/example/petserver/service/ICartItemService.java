package com.example.petserver.service;
import com.example.petserver.entity.CartItem;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface ICartItemService extends IService<CartItem> {
	// 获取某人的购物车列表（包含商品详情）
	List<CartItem> getMyCart(Long userId);

	// 结算购物车（把选中的购物车项变成订单）
	boolean checkout(List<Long> cartIds, Long userId, String address);
}