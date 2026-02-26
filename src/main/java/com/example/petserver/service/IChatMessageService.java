package com.example.petserver.service;

import com.example.petserver.entity.ChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * <p>
 * 客服聊天记录表 服务类
 * </p>
 *
 * @author TAO
 * @since 2025-12-23
 */
public interface IChatMessageService extends IService<ChatMessage> {
	List<ChatMessage> getChatHistory(Long userId1, Long userId2, String bizType);

	void markAsRead(Long senderId, Long receiverId);
}
