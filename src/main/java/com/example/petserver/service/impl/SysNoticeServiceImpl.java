package com.example.petserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.petserver.entity.SysNotice;
import com.example.petserver.mapper.SysNoticeMapper;
import com.example.petserver.service.ISysNoticeService;
import com.example.petserver.websocket.WebSocketServer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements ISysNoticeService {

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean batchSend(List<Long> userIds, String title, String content) {
		if (userIds == null || userIds.isEmpty()) return false;

		// 去重
		List<Long> uniqueUserIds = userIds.stream().distinct().collect(Collectors.toList());

		List<SysNotice> notices = new ArrayList<>();
		for (Long uid : uniqueUserIds) {
			SysNotice notice = new SysNotice();
			notice.setUserId(uid);
			notice.setTitle(title);
			notice.setContent(content);
			notice.setCreateTime(LocalDateTime.now());
			notice.setIsRead(0); // 默认为未读
			notices.add(notice);
		}

		boolean success = this.saveBatch(notices);
		if (success) {
			// 保存成功后，通过 WebSocket 实时推送
			for (Long uid : uniqueUserIds) {
				try {
					WebSocketServer.sendInfo("NEW_NOTICE", uid);
				} catch (Exception e) {
					// 推送失败仅记录日志，不影响主流程
					e.printStackTrace();
				}
			}
		}
		return success;
	}
}
