package com.example.gaokaoproject.service;

import com.example.gaokaoproject.entity.AiConversation;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI 对话服务（大模型交互层）
 */
public interface AiChatService {

    /**
     * 发起新对话 —— 发送用户问题，返回 AI 回复（非流式）
     */
    List<AiConversation> chat(Long userId, String sessionId, String userMessage);

    /**
     * 流式对话 —— 返回 SseEmitter，逐 token 推送
     */
    SseEmitter chatStream(Long userId, String sessionId, String userMessage);
}
