package com.kimting.kimting.memory.presentation;

import com.kimting.kimting.memory.port.in.ChatUseCase;
import com.kimting.kimting.memory.presentation.dto.ChatRequest;
import com.kimting.kimting.memory.presentation.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatUseCase chatUseCase;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            throw new IllegalArgumentException("'message' field must not be blank.");
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = (auth != null && auth.getPrincipal() instanceof UUID) ? (UUID) auth.getPrincipal() : null;
        return ResponseEntity.ok(chatUseCase.chat(request, userId));
    }
}
