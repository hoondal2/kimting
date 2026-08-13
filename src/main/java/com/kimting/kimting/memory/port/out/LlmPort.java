package com.kimting.kimting.memory.port.out;

public interface LlmPort {

    String chat(String systemPrompt, String userMessage);
}
