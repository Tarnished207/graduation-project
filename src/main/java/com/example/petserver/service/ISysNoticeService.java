package com.example.petserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.petserver.entity.SysNotice;

import java.util.List;

public interface ISysNoticeService extends IService<SysNotice> {
	boolean batchSend(List<Long> userIds, String title, String content);
}
