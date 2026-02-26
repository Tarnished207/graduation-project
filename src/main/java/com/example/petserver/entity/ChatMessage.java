package com.example.petserver.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("chat_message")
public class ChatMessage implements Serializable {
	@TableId(type = IdType.AUTO)
	private Long id;
	private Long senderId;
	private Long receiverId;
	private String content;
	private String msgType;
	private String bizType;
	private LocalDateTime createTime;
	private Integer isRead; // 0:未读, 1:已读

	// --- 手动补全 Getter/Setter ---
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	public Long getSenderId() { return senderId; }
	public void setSenderId(Long senderId) { this.senderId = senderId; }
	public Long getReceiverId() { return receiverId; }
	public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
	public String getContent() { return content; }
	public void setContent(String content) { this.content = content; }
	public String getMsgType() { return msgType; }
	public void setMsgType(String msgType) { this.msgType = msgType; }
	public String getBizType() { return bizType; }
	public void setBizType(String bizType) { this.bizType = bizType; }
	public LocalDateTime getCreateTime() { return createTime; }
	public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

	public Integer getIsRead() {
		return isRead;
	}

	public void setIsRead(Integer isRead) {
		this.isRead = isRead;
	}
}