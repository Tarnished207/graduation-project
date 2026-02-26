package com.example.petserver.service;

import com.example.petserver.entity.Product;
import com.example.petserver.vo.ProductRecommendVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 商品表 服务类
 * </p>
 *
 * @author TAO
 * @since 2025-12-23
 */
public interface IProductService extends IService<Product> {
	/**
	 * 基于用户宠物画像的商品推荐（汇总所有宠物）
	 *
	 * @param userId 用户ID
	 * @return 推荐商品列表
	 */
	List<Product> getRecommendProducts(Long userId);

	/**
	 * 基于用户宠物画像的商品推荐（带推荐理由）
	 *
	 * @param userId 用户ID
	 * @return 推荐VO列表
	 */
	List<ProductRecommendVO> getRecommendProductsVO(Long userId);

	/**
	 * 按指定宠物生成专属推荐
	 *
	 * @param userId 用户ID（用于购买去重与分类偏好补充）
	 * @param petId  宠物ID
	 * @return 推荐商品列表
	 */
	List<Product> getRecommendProductsByPet(Long userId, Long petId);

	List<ProductRecommendVO> getRecommendProductsByPetVO(Long userId, Long petId);
}
