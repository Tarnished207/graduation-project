package com.example.petserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("cart_item")
public class CartItem implements Serializable {
	@TableId(type = IdType.AUTO)
	private Long id;
	private Long userId;
	private Long productId;
	private Integer quantity;
	private LocalDateTime createTime;

	// --- 下面这些字段数据库里没有，是为了传给前端方便展示用的 ---
	@TableField(exist = false)
	private String productName;

	@TableField(exist = false)
	private BigDecimal price;

	@TableField(exist = false)
	private String description; // 商品描述/图片

	@TableField(exist = false)
	private String image;

	// --- 手动 Getter/Setter ---
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public Long getUserId() { return userId; }
	public void setUserId(Long userId) { this.userId = userId; }
	public Long getProductId() { return productId; }
	public void setProductId(Long productId) { this.productId = productId; }
	public Integer getQuantity() { return quantity; }
	public void setQuantity(Integer quantity) { this.quantity = quantity; }
	public LocalDateTime getCreateTime() { return createTime; }
	public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

	// 额外字段的 Getter/Setter
	public String getProductName() { return productName; }
	public void setProductName(String productName) { this.productName = productName; }
	public BigDecimal getPrice() { return price; }
	public void setPrice(BigDecimal price) { this.price = price; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}
}