package com.example.petserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("sys_notice")
public class SysNotice implements Serializable {
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;
	private String title;
	private String content;
	private LocalDateTime createTime;

	@JsonSerialize(using = ToStringSerializer.class)
	private Long userId;
	/**
	 * 0: Unread, 1: Read
	 */
	private Integer isRead;

	// Getter & Setter
	public Integer getId() { return id; }
	public void setId(Integer id) { this.id = id; }
	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }
	public String getContent() { return content; }
	public void setContent(String content) { this.content = content; }
	public LocalDateTime getCreateTime() { return createTime; }
	public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Integer getIsRead() {
		return isRead;
	}

	public void setIsRead(Integer isRead) {
		this.isRead = isRead; }
}