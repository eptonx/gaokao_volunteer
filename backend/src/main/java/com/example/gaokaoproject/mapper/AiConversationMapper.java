package com.example.gaokaoproject.mapper;

import com.example.gaokaoproject.entity.AiConversation;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface AiConversationMapper {

    // 查询用户的所有对话记录
    @Select("SELECT * FROM ai_conversation WHERE user_id = #{userId} ORDER BY created_at ASC")
    List<AiConversation> selectByUserId(@Param("userId") Long userId);

    // 查询某个会话的所有消息
    @Select("SELECT * FROM ai_conversation WHERE session_id = #{sessionId} ORDER BY created_at ASC")
    List<AiConversation> selectBySessionId(@Param("sessionId") String sessionId);

    // 根据ID查询单条对话
    @Select("SELECT * FROM ai_conversation WHERE id = #{id}")
    AiConversation selectById(@Param("id") Long id);

    // 插入一条对话记录（注意：id自增，created_at数据库自动生成）
    @Insert("INSERT INTO ai_conversation(user_id, session_id, role, content, created_at) " +
            "VALUES(#{userId}, #{sessionId}, #{role}, #{content}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Integer insertAiConversation(AiConversation conversation);

    // 删除某个会话的所有消息
    @Delete("DELETE FROM ai_conversation WHERE session_id = #{sessionId}")
    Integer deleteBySessionId(@Param("sessionId") String sessionId);

    // 查询用户的所有会话ID列表（去重，按最新消息时间倒序）
    @Select("SELECT DISTINCT session_id, MAX(created_at) AS last_msg_time " +
            "FROM ai_conversation WHERE user_id = #{userId} " +
            "GROUP BY session_id ORDER BY last_msg_time DESC")
    List<Map<String, Object>> selectSessionsByUserId(@Param("userId") Long userId);
}