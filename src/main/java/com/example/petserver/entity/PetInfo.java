package com.example.petserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.math.BigDecimal;

@TableName("pet_info")
public class PetInfo implements Serializable {
	@TableId(type = IdType.AUTO)
	private Long id;
	private Long userId;
	private String nickname;
	private String breed;
	private Integer type; // 0-狗, 1-猫
	private Integer sex;  // 0-公, 1-母
	private BigDecimal weight;
	private Integer age;
	private String avatar;

	// === 新增字段 (对应数据库修改) ===

	/**
	 * 生日 (格式: yyyy-MM-dd)
	 */
	private String birthday;

	/**
	 * 绝育状态 (0:未绝育, 1:已绝育)
	 * 原数据库字段: is_sterilized -> 改为 sterilization
	 */
	private Integer sterilization;

	/**
	 * 健康状态 (0:健康, 1:生病, 2:治疗中)
	 * 原数据库类型: varchar -> 改为 tinyint
	 */
	private Integer healthStatus;

	/**
	 * 下次疫苗接种时间 (格式: yyyy-MM-dd)
	 */
	private String nextVaccine;

	// === 辅助字段 (数据库中不存在) ===
	@TableField(exist = false)
	private String ownerName;

	// === Getters & Setters ===

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public Long getUserId() { return userId; }
	public void setUserId(Long userId) { this.userId = userId; }
	public String getNickname() { return nickname; }
	public void setNickname(String nickname) { this.nickname = nickname; }
	public String getBreed() { return breed; }
	public void setBreed(String breed) { this.breed = breed; }
	public Integer getType() { return type; }
	public void setType(Integer type) { this.type = type; }
	public Integer getSex() { return sex; }
	public void setSex(Integer sex) { this.sex = sex; }
	public BigDecimal getWeight() { return weight; }
	public void setWeight(BigDecimal weight) { this.weight = weight; }
	public Integer getAge() { return age; }
	public void setAge(Integer age) { this.age = age; }
	public String getAvatar() { return avatar; }
	public void setAvatar(String avatar) { this.avatar = avatar; }

	// 新增字段的 Getter/Setter
	public String getBirthday() { return birthday; }
	public void setBirthday(String birthday) { this.birthday = birthday; }

	public Integer getSterilization() { return sterilization; }
	public void setSterilization(Integer sterilization) { this.sterilization = sterilization; }

	public Integer getHealthStatus() { return healthStatus; }
	public void setHealthStatus(Integer healthStatus) { this.healthStatus = healthStatus; }

	public String getNextVaccine() { return nextVaccine; }
	public void setNextVaccine(String nextVaccine) { this.nextVaccine = nextVaccine; }

	public String getOwnerName() { return ownerName; }
	public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
}