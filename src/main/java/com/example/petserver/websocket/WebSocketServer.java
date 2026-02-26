package com.example.petserver.websocket;

import com.alibaba.fastjson.JSON;
import com.example.petserver.entity.ChatMessage;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket服务端
 * 路径：/ws/chat/{userId}
 */
@ServerEndpoint("/ws/chat/{userId}")
@Component
@Slf4j
public class WebSocketServer {

	/**
	 * 存放每个客户端对应的WebSocketServer对象
	 * Key: userId, Value: WebSocketServer
	 */
	private static ConcurrentHashMap<Long, WebSocketServer> webSocketMap = new ConcurrentHashMap<>();

	/**
	 * 与某个客户端的连接会话，需要通过它来给客户端发送数据
	 */
	private Session session;

	/**
	 * 接收userId
	 */
	private Long userId;

	/**
	 * 连接建立成功调用的方法
	 */
	@OnOpen
	public void onOpen(Session session, @PathParam("userId") String userIdStr) {
		this.session = session;
		try {
			this.userId = Long.parseLong(userIdStr);
		} catch (NumberFormatException e) {
			log.warn("WebSocket连接失败，无效的用户ID: {}", userIdStr);
			return;
		}

		if (webSocketMap.containsKey(userId)) {
			webSocketMap.remove(userId);
			webSocketMap.put(userId, this);
		} else {
			webSocketMap.put(userId, this);
		}
		log.info("用户连接:{}，当前在线人数为:{}", userId, webSocketMap.size());
	}

	/**
	 * 连接关闭调用的方法
	 */
	@OnClose
	public void onClose() {
		if (userId != null && webSocketMap.containsKey(userId)) {
			webSocketMap.remove(userId);
			log.info("用户退出:{}，当前在线人数为:{}", userId, webSocketMap.size());
		}
	}

	/**
	 * 收到客户端消息后调用的方法
	 * (通常我们的聊天消息走HTTP接口存库后再推送到WS，这里主要处理心跳等简单消息)
	 */
	@OnMessage
	public void onMessage(String message, Session session) {
		log.info("用户消息:{}，报文:{}", userId, message);
		// 如果需要处理客户端直接发的ws消息，可以在这里写逻辑
		// 例如心跳检测回复
		if ("ping".equals(message)) {
			try {
				this.sendMessage("pong");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 发生错误时调用
	 */
	@OnError
	public void onError(Session session, Throwable error) {
		log.error("用户错误:{}，原因:{}", this.userId, error.getMessage());
		error.printStackTrace();
	}

	/**
	 * 实现服务器主动推送
	 */
	public void sendMessage(String message) throws IOException {
		this.session.getBasicRemote().sendText(message);
	}

	/**
	 * 发送自定义消息给指定用户
	 */
	public static void sendInfo(String message, Long toUserId) throws IOException {
		log.info("发送消息到:{}，报文:{}", toUserId, message);
		if (webSocketMap.containsKey(toUserId)) {
			webSocketMap.get(toUserId).sendMessage(message);
		} else {
			log.info("用户{}不在线，消息未实时推送", toUserId);
		}
	}

	/**
	 * 发送消息对象给指定用户
	 */
	public static void sendObject(ChatMessage chatMessage, Long toUserId) {
		try {
			String jsonStr = JSON.toJSONString(chatMessage);
			sendInfo(jsonStr, toUserId);
		} catch (IOException e) {
			log.error("推送消息失败: {}", e.getMessage());
		}
	}
}
