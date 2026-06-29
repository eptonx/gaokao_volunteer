package com.example.gaokaoproject.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    /**
     * 高考志愿填报 AI 助手 —— 系统提示词
     */
    private static final String SYSTEM_PROMPT = """
            你是高考志愿填报AI助手，具备以下能力：
            1. 根据考生成绩、位次、省份、选科等信息，推荐合适的院校和专业
            2. 分析历年录取数据，评估录取概率
            3. 生成"冲、稳、保"梯度志愿方案
            4. 解答院校选择、专业方向、就业前景等问题

            回答原则：
            - 基于真实数据给出建议，不确定时说明"参考往年数据可能存在波动"
            - 冲稳保划分标准：冲刺（录取概率30%-60%）、稳妥（60%-85%）、保底（85%-99%）
            - 推荐院校时，必须严格按「3所冲刺 + 4所稳妥 + 3所保底」梯度结构，共10所院校，缺一不可
            - 每所院校列出 1~6 个具体专业（不同院校专业数量要有差异，不要都是固定数量），格式：院校名（梯度）- 专业1（概率%）- 专业2（概率%）...
            - 每次回答控制在800字以内，确保10所院校全部列出
            - 使用友好的语气，适当使用emoji
            """;

    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
}