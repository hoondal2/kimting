package com.kimting.kimting.api;

import com.kimting.kimting.api.dto.ChatRequest;
import com.kimting.kimting.api.dto.ChatResponse;
import com.kimting.kimting.core.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 사용자 메시지를 받아 관련 기억을 검색하고 AI 응답을 반환한다.
     * 메시지에 저장 가능한 정보가 있으면 Memory로 자동 저장된다.
     *
     * POST /api/chat
     * { "message": "나 내일 10시에 회의있어", "sessionId": "optional" }
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            throw new IllegalArgumentException("'message' field must not be blank.");
        }
        return ResponseEntity.ok(chatService.chat(request));
    }
}
