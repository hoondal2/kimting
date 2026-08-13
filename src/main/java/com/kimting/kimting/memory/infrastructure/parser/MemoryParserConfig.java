package com.kimting.kimting.memory.infrastructure.parser;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MemoryParserConfig {

    @Bean
    @ConditionalOnProperty(name = "kimting.parser.provider", havingValue = "llm")
    public MemoryParser llmMemoryParser(ChatModel chatModel) {
        return new LlmMemoryParser(chatModel);
    }

    @Bean
    @ConditionalOnMissingBean(MemoryParser.class)
    public MemoryParser ruleBasedMemoryParser() {
        return new RuleBasedMemoryParser();
    }
}
