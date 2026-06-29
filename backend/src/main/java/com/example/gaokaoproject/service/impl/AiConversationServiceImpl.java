package com.example.gaokaoproject.service.impl;

import com.example.gaokaoproject.entity.AiConversation;
import com.example.gaokaoproject.mapper.AiConversationMapper;
import com.example.gaokaoproject.service.AiConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AiConversationServiceImpl implements AiConversationService {

    @Autowired
    private AiConversationMapper aiConversationMapper;

    @Override
    public List<AiConversation> selectByUserId(Long userId) {
        return aiConversationMapper.selectByUserId(userId);
    }

    @Override
    public List<AiConversation> selectBySessionId(String sessionId) {
        return aiConversationMapper.selectBySessionId(sessionId);
    }

    @Override
    public AiConversation selectById(Long id) {
        return aiConversationMapper.selectById(id);
    }

    @Override
    public Integer insertAiConversation(AiConversation conversation) {
        return aiConversationMapper.insertAiConversation(conversation);
    }

    @Override
    public Integer deleteBySessionId(String sessionId) {
        return aiConversationMapper.deleteBySessionId(sessionId);
    }

    @Override
    public List<Map<String, Object>> selectSessionsByUserId(Long userId) {
        return aiConversationMapper.selectSessionsByUserId(userId);
    }
}