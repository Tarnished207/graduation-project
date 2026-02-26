package com.example.petserver.mapper;

import com.example.petserver.entity.ChatMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 客服聊天记录表 Mapper 接口
 * </p>
 *
 * @author TAO
 * @since 2025-12-23
 */
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

}
