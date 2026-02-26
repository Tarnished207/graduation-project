package com.example.petserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat; // 1. 记得导入这个
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime; // 2. 记得导入这个

@Data
@TableName("feed_plan")
public class FeedPlan implements Serializable {

	@TableId(type = IdType.AUTO)
	private Long id;
	private Long userId;
	private Long petId;
	private String planName;
	private String planTime;
	private String foodName;
	private String amount;

	/**
	 * 周期设置:
	 * 0: 每天 (默认)
	 * 1: 周一, 2: 周二 ... 7: 周日
	 */
	private Integer weekDay;

	private Integer status;

	// === 【新增】记录最后一次操作的时间 ===
	// 3. 加上这个字段，报错就会消失了
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private LocalDateTime updateTime;

	// --- 为了防止 Lombok 不生效，我帮你把手动 Getter/Setter 也补全了 ---

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Long getUserId() { return userId; }
	public void setUserId(Long userId) { this.userId = userId; }

	public Long getPetId() { return petId; }
	public void setPetId(Long petId) { this.petId = petId; }

	public String getPlanName() { return planName; }
	public void setPlanName(String planName) { this.planName = planName; }

	public String getPlanTime() { return planTime; }
	public void setPlanTime(String planTime) { this.planTime = planTime; }

	public String getFoodName() { return foodName; }
	public void setFoodName(String foodName) { this.foodName = foodName; }

	public String getAmount() { return amount; }

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public Integer getWeekDay() {
		return weekDay;
	}

	public void setWeekDay(Integer weekDay) {
		this.weekDay = weekDay;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	// === 核心修复：updateTime 的 Getter/Setter ===
	public LocalDateTime getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(LocalDateTime updateTime) {
		this.updateTime = updateTime;
	}
}