package com.kimting.kimting.parser;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MemoryParserConfig {

    /**
     * kimting.parser.provider=llm 일 때 LLM 기반 파서 사용.
     * 현재는 Google Gemini (spring-ai-starter-model-openai + Google AI Studio 엔드포인트).
     * application.yaml 에 spring.ai.openai.api-key 가 있어야 한다.
     *
     * Spring AI 의 ChatModel 인터페이스를 사용하므로 LLM을 교체해도 이 코드는 변경 불필요.
     */
    @Bean
    @ConditionalOnProperty(name = "kimting.parser.provider", havingValue = "llm")
    public MemoryParser llmMemoryParser(ChatModel chatModel) {
        return new LlmMemoryParser(chatModel);
    }

    /**
     * LLM 파서가 등록되지 않은 경우 (provider=rule-based 또는 미설정) 자동으로 사용.
     * API key 없이도 동작한다.
     */
    @Bean
    @ConditionalOnMissingBean(MemoryParser.class)
    public MemoryParser ruleBasedMemoryParser() {
        return new RuleBasedMemoryParser();
    }
}
