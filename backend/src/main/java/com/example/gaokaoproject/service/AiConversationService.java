package com.example.gaokaoproject.service;

import com.example.gaokaoproject.entity.AiConversation;
import java.util.List;
import java.util.Map;

public interface AiConversationService {
    List<AiConversation> selectByUserId(Long userId);
    List<AiConversation> selectBySessionId(String sessionId);
    AiConversation selectById(Long id);
    Integer insertAiConversation(AiConversation conversation);
    Integer deleteBySessionId(String sessionId);
    /** 查询用户的所有会话列表（去重，返回 sessionId + 最后消息时间） */
    List<Map<String, Object>> selectSessionsByUserId(Long userId);
}