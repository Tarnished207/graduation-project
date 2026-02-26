package com.example.petserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 健康记录实体类 (最终修正版)
 * 对应数据库表: health_record
 */
@TableName("health_record")
public class HealthRecord implements Serializable {

	private static final long serialVersionUID = 1L;

	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	private Long petId;

	// 【⭐核心修复】必须叫 type，对应数据库的 type 列
	// 千万不要改成 recordType，否则必报 500 错误
	private String type;

	private String title;
	private String description;
	private String hospital;
	private String doctor;
	private String img;

	// 【⭐补全字段】加上这两个，否则存体重和花费会报错
	private BigDecimal weight;
	private BigDecimal cost;

	// 【⭐补全字段】加上日期格式化
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
	private Date nextTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private LocalDateTime createTime;

	// ==========================================
	//       Getter 和 Setter
	// ==========================================
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Long getPetId() { return petId; }
	public void setPetId(Long petId) { this.petId = petId; }

	// Getter方法名必须是 getType
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }

	public String getHospital() {
		return hospital;
	}

	public void setHospital(String hospital) {
		this.hospital = hospital;
	}

	public String getDoctor() {
		return doctor;
	}

	public void setDoctor(String doctor) {
		this.doctor = doctor;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public BigDecimal getWeight() {
		return weight;
	}

	public void setWeight(BigDecimal weight) {
		this.weight = weight;
	}

	public BigDecimal getCost() { return cost; }
	public void setCost(BigDecimal cost) { this.cost = cost; }

	public Date getNextTime() {
		return nextTime;
	}

	public void setNextTime(Date nextTime) {
		this.nextTime = nextTime;
	}

	public LocalDateTime getCreateTime() {
		return createTime;
	}

	public void setCreateTime(LocalDateTime createTime) {
		this.createTime = createTime;
	}
}