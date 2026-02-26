package com.example.petserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("product")
public class Product implements Serializable {
	private static final long serialVersionUID = 1L;

	@TableId(type = IdType.AUTO)
	private Long id;

	private String name;          // 商品名称
	private String category;      // 分类
	private BigDecimal price;     // 价格
	private Integer stock;        // 库存

	// --- [核心修改] 新增字段，与数据库对齐 ---
	private String image;         // 专门存图片URL
	private String swiperImages;  // 轮播图URL (JSON/逗号分隔)
	private String description;   // 专门存商品文案
	private String content;       // 点击后在弹窗/新页面展示的长文
	private String tags;          // 标签 (如: 幼犬,护毛)
	private String brand;         // 品牌
	private BigDecimal originalPrice; // 原价
	private String specs;         // 规格参数 (JSON)
	private Integer status;       // 状态 (1上架 0下架)
	private LocalDateTime createTime; // 创建时间

	// --- Getter & Setter ---
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getCategory() { return category; }
	public void setCategory(String category) { this.category = category; }
	public BigDecimal getPrice() { return price; }
	public void setPrice(BigDecimal price) { this.price = price; }
	public Integer getStock() { return stock; }
	public void setStock(Integer stock) { this.stock = stock; }

	// 新增字段的 Getter/Setter
	public String getSwiperImages() {
		return swiperImages;
	}

	public void setSwiperImages(String swiperImages) {
		this.swiperImages = swiperImages;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public BigDecimal getOriginalPrice() {
		return originalPrice;
	}

	public void setOriginalPrice(BigDecimal originalPrice) {
		this.originalPrice = originalPrice;
	}

	public String getSpecs() {
		return specs;
	}

	public void setSpecs(String specs) {
		this.specs = specs;
	}

	public String getImage() { return image; }
	public void setImage(String image) { this.image = image; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public String getTags() { return tags; }
	public void setTags(String tags) { this.tags = tags; }
	public Integer getStatus() { return status; }
	public void setStatus(Integer status) { this.status = status; }
	public LocalDateTime getCreateTime() { return createTime; }
	public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}