package com.example.petserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField; // [新增]
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("orders")
public class Orders implements Serializable {
	private static final long serialVersionUID = 1L;

	@TableId(value = "id", type = IdType.AUTO)
	private Long id;
	private String orderNo;
	private Long userId;
	private Long productId;
	private Integer quantity;
	private BigDecimal totalAmount;
	private Integer status; // 0-待支付, 1-已支付, 2-已发货, 3-已完成, -1-已取消
	private String address;
	private LocalDateTime createTime;
	// [新增] 商品图片 (前端展示用)



	// --- [新增] 前端显示专用字段 (数据库里没有) ---
	@TableField(exist = false)
	private String productName;
	@TableField(exist = false)
	private String productImage;
	@TableField(exist = false)
	private String productDescription;
	@TableField(exist = false)
	private String buyerName;  // 买家昵称
	@TableField(exist = false)
	private String buyerPhone; // 买家电话 (方便联系)

	// --- Getter/Setter ---
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public String getOrderNo() { return orderNo; }
	public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
	public Long getUserId() { return userId; }
	public void setUserId(Long userId) { this.userId = userId; }
	public Long getProductId() { return productId; }
	public void setProductId(Long productId) { this.productId = productId; }
	public Integer getQuantity() { return quantity; }
	public void setQuantity(Integer quantity) { this.quantity = quantity; }
	public BigDecimal getTotalAmount() { return totalAmount; }
	public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
	public Integer getStatus() { return status; }
	public void setStatus(Integer status) { this.status = status; }
	public String getAddress() { return address; }
	public void setAddress(String address) { this.address = address; }
	public LocalDateTime getCreateTime() { return createTime; }
	public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

	// [新增] 额外字段的 Getter/Setter
	public String getProductName() { return productName; }
	public void setProductName(String productName) { this.productName = productName; }
	public String getProductDescription() { return productDescription; }
	public void setProductDescription(String productDescription) { this.productDescription = productDescription; }
	public String getProductImage() { return productImage; }
	public void setProductImage(String productImage) { this.productImage = productImage; }
	public String getBuyerName() { return buyerName; }
	public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
	public String getBuyerPhone() { return buyerPhone; }
	public void setBuyerPhone(String buyerPhone) { this.buyerPhone = buyerPhone; }
}