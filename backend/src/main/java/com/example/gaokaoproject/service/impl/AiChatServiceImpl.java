package com.example.gaokaoproject.service.impl;

import com.example.gaokaoproject.entity.AiConversation;
import com.example.gaokaoproject.entity.StudentScore;
import com.example.gaokaoproject.mapper.AiConversationMapper;
import com.example.gaokaoproject.mapper.StudentScoreMapper;
import com.example.gaokaoproject.service.AiChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AI 对话服务实现 —— 接入 DeepSeek 大模型（Spring AI）
 */
@Service
public class AiChatServiceImpl implements AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatServiceImpl.class);

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private AiConversationMapper aiConversationMapper;

    @Autowired
    private StudentScoreMapper studentScoreMapper;

    // ==================== 非流式对话 ====================

    @Override
    public List<AiConversation> chat(Long userId, String sessionId, String userMessage) {
        List<AiConversation> result = new ArrayList<>();

        AiConversation userMsg = saveUserMessage(userId, sessionId, userMessage);
        result.add(userMsg);

        String contextPrompt = buildContextPrompt(userId, userMessage);

        String aiReply = callAi(contextPrompt);
        AiConversation aiMsg = saveAiMessage(userId, sessionId, aiReply);
        result.add(aiMsg);

        return result;
    }

    // ==================== 流式对话（SSE） ====================

    @Override
    public SseEmitter chatStream(Long userId, String sessionId, String userMessage) {
        SseEmitter emitter = new SseEmitter(600_000L);

        // 先保存用户消息
        saveUserMessage(userId, sessionId, userMessage);

        String contextPrompt = buildContextPrompt(userId, userMessage);

        // 异步执行：先调 AI 拿到完整回复，再逐字符推送给前端
        executor.execute(() -> {
            try {
                // 1. 发送 sessionId
                emitter.send(SseEmitter.event().name("session").data(sessionId));

                // 2. 阻塞调用 AI（不依赖 Flux）
                String fullReply = callAi(contextPrompt);
                log.info("AI 回复完成: userId={}, len={}", userId, fullReply.length());

                // 3. 逐字符推流（模拟打字效果）
                for (int i = 0; i < fullReply.length(); i++) {
                    String ch = String.valueOf(fullReply.charAt(i));
                    emitter.send(SseEmitter.event().name("message").data(ch));
                    // 中文每字延迟 40ms，英文每字延迟 15ms
                    Thread.sleep(isChinese(fullReply.charAt(i)) ? 40 : 15);
                }

                // 4. 保存 AI 回复到 DB
                saveAiMessage(userId, sessionId, fullReply);

                // 5. 完成
                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                emitter.complete();

            } catch (Exception e) {
                log.error("SSE 流式输出异常: {}", e.getMessage(), e);
                try {
                    emitter.send(SseEmitter.event().name("error").data("服务异常：" + e.getMessage()));
                    emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                    emitter.complete();
                } catch (IOException ignored) {}
            }
        });

        emitter.onTimeout(() -> {
            log.warn("SSE 超时: userId={}", userId);
            emitter.complete();
        });
        emitter.onError(t -> log.error("SSE 异常: userId={}", userId, t));

        return emitter;
    }

    // ==================== 核心：调用 AI ====================

    /**
     * 调用 DeepSeek API（通过 Spring AI ChatClient）
     */
    private String callAi(String prompt) {
        try {
            String content = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            if (content == null || content.isBlank()) {
                return "(AI 返回了空内容，请重试)";
            }
            return content;
        } catch (Exception e) {
            log.error("AI 调用失败: {}", e.getMessage(), e);
            // 返回友好的降级回复
            return "抱歉，AI 服务暂时不可用。\n\n"
                    + "可能的原因：\n"
                    + "1. API Key 无效或已过期\n"
                    + "2. 网络连接异常\n"
                    + "3. 服务端限流\n\n"
                    + "请稍后重试，或联系管理员检查 DeepSeek API 配置。\n"
                    + "错误详情：" + e.getMessage();
        }
    }

    // ==================== 辅助方法 ====================

    private AiConversation saveUserMessage(Long userId, String sessionId, String content) {
        AiConversation msg = new AiConversation();
        msg.setUserId(userId);
        msg.setSessionId(sessionId);
        msg.setRole(1);
        msg.setContent(content);
        aiConversationMapper.insertAiConversation(msg);
        return msg;
    }

    private AiConversation saveAiMessage(Long userId, String sessionId, String content) {
        AiConversation msg = new AiConversation();
        msg.setUserId(userId);
        msg.setSessionId(sessionId);
        msg.setRole(2);
        msg.setContent(content);
        aiConversationMapper.insertAiConversation(msg);
        return msg;
    }

    private String buildContextPrompt(Long userId, String userMessage) {
        StringBuilder prompt = new StringBuilder();

        List<StudentScore> scores = studentScoreMapper.selectByUserId(userId);
        if (scores != null && !scores.isEmpty()) {
            StudentScore latest = scores.get(0);
            prompt.append("【考生信息】\n");
            prompt.append("- 省份代码：").append(ns(latest.getProvinceCode())).append("\n");
            prompt.append("- 考试年份：").append(latest.getExamYear()).append("\n");
            prompt.append("- 总分：").append(latest.getTotalScore()).append("分\n");
            prompt.append("- 全省位次：第").append(latest.getProvinceRank()).append("名\n");
            if (latest.getSelectedSubjects() != null) {
                prompt.append("- 选考科目：").append(latest.getSelectedSubjects()).append("\n");
            }
            prompt.append("\n请基于以上考生信息回答。\n");
            prompt.append("注意：涉及院校推荐时，必须输出完整的「3所冲刺+4所稳妥+3所保底=10所院校」，每所列出1~6个具体专业及录取概率。\n\n");
        }

        prompt.append(userMessage);
        return prompt.toString();
    }

    private static String ns(String s) {
        return s == null ? "未知" : s;
    }

    private static boolean isChinese(char c) {
        return Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN;
    }
}
