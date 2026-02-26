package com.example.petserver.service;

import com.example.petserver.entity.Orders;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author TAO
 * @since 2025-12-23
 */
public interface IOrdersService extends IService<Orders> {
	//查看某人的所有订单
	List<Orders> getByUserId(Long userId);
}
