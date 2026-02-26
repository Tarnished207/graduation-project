package com.example.petserver.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.petserver.entity.Product;
import com.example.petserver.vo.ProductRecommendVO;
import com.example.petserver.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

import com.example.petserver.common.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
/**
 * <p>
 * 商品表 前端控制器
 * </p>
 *
 * @author TAO
 * @since 2025-12-23
 */
@RestController
@RequestMapping("/product")
public class ProductController {

	@Autowired
	private IProductService productService;
	//上架新商品 (管理员用)
	@PostMapping("/add")
	public boolean add(@RequestBody Product product) {
		return productService.save(product);
	}

	// 升级版查询接口：支持 多条件组合搜索
	// URL: GET /product/list?name=xxx&category=food
	@GetMapping("/list")
	public List<Product> list(
			@RequestParam(required = false) String name,
			@RequestParam(required = false) String category
	) {
		com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Product> query = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
		// 1. 如果传了名称，就模糊查询
		if (name != null && !name.isEmpty()) {
			query.like("name", name);
		}
		// 2. 如果传了分类，就精准查询 (比如只看 'food')
		if (category != null && !category.isEmpty()) {
			query.eq("category", category);
		}
		// 3. 默认按 ID 倒序（新上架的在前）
		query.orderByDesc("id");
		return productService.list(query);
	}

	// --- [新增] 修改商品信息 ---
	@PostMapping("/update")
	public boolean update(@RequestBody Product product) {
		return productService.updateById(product);
	}

	// --- [新增] 删除商品 ---
	@GetMapping("/delete/{id}")
	public boolean delete(@PathVariable Long id) {
		return productService.removeById(id);
	}

	// --- [新增] 库存管理功能 (管理员专用) ---

	// 1. 快速补货接口 (入库)
	// URL: POST http://localhost:9090/product/addStock
	@PostMapping("/addStock")
	public boolean addStock(@RequestBody StockRequest request) {
		Product product = productService.getById(request.getId());
		if (product == null) return false;

		// 原库存 + 新进货数量
		product.setStock(product.getStock() + request.getNum());

		return productService.updateById(product);
	}

	// 2. 库存预警查询 (查出库存 < 10 的商品)
	// URL: GET http://localhost:9090/product/lowStock
	@GetMapping("/lowStock")
	public List<Product> getLowStock() {
		com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Product> wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
		// 查询库存小于 10 的商品
		wrapper.lt("stock", 10);
		return productService.list(wrapper);
	}

	// 辅助类：用于接收补货参数
	public static class StockRequest {
		private Long id;   // 商品ID
		private Integer num; // 进货数量

		public Long getId() { return id; }
		public void setId(Long id) { this.id = id; }
		public Integer getNum() { return num; }
		public void setNum(Integer num) { this.num = num; }
	}

	@GetMapping("/stats")
	public Map<String, Object> stats() {
		Map<String, Object> map = new HashMap<>();

		// 1. 商品总数
		map.put("total", productService.count());

		// 2. 库存紧张 (Stock < 10)
		map.put("lowStock", productService.count(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Product>().lt("stock", 10)));

		// 3. 上架中 (Status = 1)
		try {
			map.put("active", productService.count(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Product>().eq("status", 1)));
		} catch (Exception e) {
			map.put("active", 0);
		}

		// 4. 分类数量
		long categoryCount = productService.list().stream().map(Product::getCategory).distinct().count();
		map.put("categoryCount", categoryCount);

		// 5. [新增] 本周新品 (CreateTime > 7天前)
		try {
			com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Product> weekWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
			weekWrapper.ge("create_time", LocalDateTime.now().minusDays(7));
			map.put("newThisWeek", productService.count(weekWrapper));
		} catch (Exception e) {
			map.put("newThisWeek", 0);
		}

		return map;
	}

	@GetMapping("/recommend")
	public Result getRecommend(@RequestParam Long userId) {
		List<ProductRecommendVO> list = productService.getRecommendProductsVO(userId);
		return Result.success(list);
	}

	// 新增：按宠物专属推荐
	@GetMapping("/recommend/pet")
	public Result getRecommendByPet(@RequestParam Long userId, @RequestParam Long petId) {
		List<ProductRecommendVO> list = productService.getRecommendProductsByPetVO(userId, petId);
		return Result.success(list);
	}

	@GetMapping("/{id}")
	public Product getById(@PathVariable Long id) {
		return productService.getById(id);
	}

}
