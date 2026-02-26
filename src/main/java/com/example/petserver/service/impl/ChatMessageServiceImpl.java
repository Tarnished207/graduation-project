package com.example.petserver.service.impl;

import com.example.petserver.entity.ChatMessage;
import com.example.petserver.mapper.ChatMessageMapper;
import com.example.petserver.service.IChatMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import java.util.List;

/**
 * <p>
 * 客服聊天记录表 服务实现类
 * </p>
 *
 * @author TAO
 * @since 2025-12-23
 */
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IChatMessageService {
	@Override
	public List<ChatMessage> getChatHistory(Long userId1, Long userId2, String bizType) {
		// 逻辑：查找 (发送者是A 且 接收者是B) 或者 (发送者是B 且 接收者是A) 的消息
		// 并且要在同一个业务频道下 (比如都是看医生的记录)
		QueryWrapper<ChatMessage> query = new QueryWrapper<>();
		query.eq("biz_type", bizType)
				.and(wrapper -> wrapper
						.nested(i -> i.eq("sender_id", userId1).eq("receiver_id", userId2))
						.or()
						.nested(i -> i.eq("sender_id", userId2).eq("receiver_id", userId1))
				)
				.orderByAsc("create_time");
		return this.list(query);
	}

	@Override
	public void markAsRead(Long senderId, Long receiverId) {
		UpdateWrapper<ChatMessage> update = new UpdateWrapper<>();
		update.set("is_read", 1)
				.eq("sender_id", senderId)
				.eq("receiver_id", receiverId)
				.eq("is_read", 0);
		this.update(update);
	}
}
