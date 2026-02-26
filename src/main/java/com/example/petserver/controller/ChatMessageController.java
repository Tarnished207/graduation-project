package com.example.petserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.example.petserver.entity.ChatMessage;
import com.example.petserver.service.IChatMessageService;
import com.example.petserver.websocket.WebSocketServer; // 引入WebSocketServer
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatMessageController {
	@Autowired
	private IChatMessageService chatService;

	@PostMapping("/send")
	public boolean send(@RequestBody ChatMessage msg) {
		msg.setCreateTime(LocalDateTime.now());
		if (msg.getMsgType() == null) msg.setMsgType("TEXT");
		msg.setIsRead(0); // 默认为未读
		
		boolean saved = chatService.save(msg);
		if (saved) {
			// 核心修改：保存成功后，通过WebSocket实时推送给接收者
			WebSocketServer.sendObject(msg, msg.getReceiverId());
		}
		return saved;
	}

	@PostMapping("/read")
	public boolean read(@RequestBody ChatMessage msg) {
		if (msg.getSenderId() == null || msg.getReceiverId() == null) return false;
		chatService.markAsRead(msg.getSenderId(), msg.getReceiverId());
		return true;
	}

	@GetMapping("/history")
	public List<ChatMessage> history(@RequestParam Long uid1, @RequestParam Long uid2, @RequestParam String type) {
		return chatService.getChatHistory(uid1, uid2, type);
	}

	// 【新增】获取某个用户的所有聊天记录 (用于生成最近联系人列表)
	// 逻辑：查出所有 我发的 或 发给我的 消息
	@GetMapping("/allMessages")
	public List<ChatMessage> allMessages(@RequestParam Long userId) {
		QueryWrapper<ChatMessage> query = new QueryWrapper<>();
		query.and(wrapper -> wrapper.eq("sender_id", userId).or().eq("receiver_id", userId));
		query.orderByDesc("create_time"); // 最新消息在前
		return chatService.list(query);
	}
}
