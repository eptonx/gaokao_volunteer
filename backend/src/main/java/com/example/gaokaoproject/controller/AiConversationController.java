package com.example.gaokaoproject.controller;

import com.example.gaokaoproject.entity.AiConversation;
import com.example.gaokaoproject.res.Result;
import com.example.gaokaoproject.service.AiChatService;
import com.example.gaokaoproject.service.AiConversationService;
import com.example.gaokaoproject.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/student/ai")
public class AiConversationController {

    @Autowired
    private AiConversationService aiConversationService;

    @Autowired
    private AiChatService aiChatService;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("未登录或 Token 格式错误");
        }
        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new RuntimeException("Token 无效或已过期，请重新登录");
        }
        return userId;
    }

    // ==================== 会话管理 ====================

    /**
     * 查询用户的会话列表（每个 sessionId 一条，按最近消息时间倒序）
     */
    @GetMapping("/sessions")
    public Result<List<Map<String, Object>>> listSessions(@RequestHeader("Authorization") String auth) {
        Long userId = getUserId(auth);
        List<Map<String, Object>> sessions = aiConversationService.selectSessionsByUserId(userId);
        return Result.ok(sessions);
    }

    /** 删除整个会话 */
    @DeleteMapping("/deleteSession")
    public Result<Integer> deleteSession(@RequestParam String sessionId) {
        Integer rows = aiConversationService.deleteBySessionId(sessionId);
        return Result.ok(rows);
    }

    // ==================== 消息查询 ====================

    /** 查询某个会话的全部消息（历史记录） */
    @GetMapping("/history")
    public Result<List<AiConversation>> historyBySession(@RequestParam String sessionId) {
        List<AiConversation> list = aiConversationService.selectBySessionId(sessionId);
        return Result.ok(list);
    }

    /** 查询用户的所有消息 */
    @GetMapping("/listByUser")
    public Result<List<AiConversation>> listByUser(@RequestParam Long userId) {
        List<AiConversation> list = aiConversationService.selectByUserId(userId);
        return Result.ok(list);
    }

    /** 查询单条消息 */
    @GetMapping("/get")
    public Result<AiConversation> getById(@RequestParam Long id) {
        AiConversation conv = aiConversationService.selectById(id);
        return Result.ok(conv);
    }

    // ==================== AI 对话 ====================

    /**
     * 发起对话（非流式）—— 发送消息，返回完整对话
     */
    @PostMapping("/chat")
    public Result<List<AiConversation>> chat(
            @RequestParam String message,
            @RequestParam(required = false) String sessionId,
            @RequestHeader("Authorization") String auth) {

        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
        }

        Long userId = getUserId(auth);
        List<AiConversation> result = aiChatService.chat(userId, sessionId, message);
        return Result.ok(result);
    }

    /**
     * 流式对话 —— SSE 推送，逐 token 返回
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @RequestParam String message,
            @RequestParam(required = false) String sessionId,
            @RequestHeader("Authorization") String auth) {

        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
        }

        Long userId = getUserId(auth);
        return aiChatService.chatStream(userId, sessionId, message);
    }

    // ==================== 辅助 ====================

    /** 手动添加对话记录（管理后台使用） */
    @PostMapping("/add")
    public Result<Long> add(@RequestBody AiConversation conversation) {
        aiConversationService.insertAiConversation(conversation);
        return Result.ok(conversation.getId());
    }
}
