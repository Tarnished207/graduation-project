package com.example.petserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petserver.entity.Orders;
import com.example.petserver.entity.Product;
import com.example.petserver.mapper.OrdersMapper;
import com.example.petserver.mapper.ProductMapper;
import com.example.petserver.service.IOrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements IOrdersService {

	@Autowired
	private ProductMapper productMapper;

	@Override
	public List<Orders> getByUserId(Long userId) {
		QueryWrapper<Orders> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("user_id", userId);
		queryWrapper.orderByDesc("create_time");

		List<Orders> ordersList = this.list(queryWrapper);

		// 遍历每个订单，把商品名字查出来填进去
		for (Orders order : ordersList) {
			Product p = productMapper.selectById(order.getProductId());
			if (p != null) {
				order.setProductName(p.getName());
				order.setProductDescription(p.getDescription());
			}
		}
		return ordersList;
	}
}