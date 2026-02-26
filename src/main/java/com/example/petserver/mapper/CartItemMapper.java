package com.example.petserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.petserver.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {

	// 【关键代码】在这里直接写 SQL，MyBatis 会自动执行
	// 这里的重点是：把 product 表的 image 字段查出来，赋值给 CartItem 的 image 属性
	@Select("SELECT c.*, " +
			"p.name as productName, " +
			"p.price as price, " +
			"p.description as description, " +
			"p.image as image " +
			"FROM cart_item c " +
			"LEFT JOIN product p ON c.product_id = p.id " +
			"WHERE c.user_id = #{userId} " +
			"ORDER BY c.create_time DESC")
	List<CartItem> getMyCart(Long userId);
}
