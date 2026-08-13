package com.kimting.kimting.memory.port.in;

import com.kimting.kimting.memory.presentation.dto.ChatRequest;
import com.kimting.kimting.memory.presentation.dto.ChatResponse;

import java.util.UUID;

public interface ChatUseCase {

    ChatResponse chat(ChatRequest request, UUID userId);
}
