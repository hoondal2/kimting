package com.kimting.kimting.parser;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MemoryParserConfig {

    /**
     * kimting.parser.provider=anthropic 일 때 Claude 기반 LLM 파서 사용.
     * application.yaml 에 spring.ai.anthropic.api-key 가 있어야 한다.
     *
     * 다른 LLM으로 교체하려면:
     *  1. build.gradle 에서 spring-ai-starter-model-anthropic → 원하는 provider로 변경
     *  2. application.yaml 에서 해당 provider 설정 추가
     *  3. kimting.parser.provider 값 변경 (havingValue 와 맞춰야 함)
     *
     * Spring AI 의 ChatModel 인터페이스를 사용하므로 LlmMemoryParser 코드는 변경 불필요.
     */
    @Bean
    @ConditionalOnProperty(name = "kimting.parser.provider", havingValue = "anthropic")
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
