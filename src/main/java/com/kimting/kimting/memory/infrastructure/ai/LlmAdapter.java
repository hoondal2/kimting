package com.kimting.kimting.memory.infrastructure.ai;

import com.kimting.kimting.memory.port.out.LlmPort;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LlmAdapter implements LlmPort {

    private final ChatModel chatModel;

    @Override
    public String chat(String systemPrompt, String userMessage) {
        return ChatClient.builder(chatModel).build()
                .prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();
    }
}
