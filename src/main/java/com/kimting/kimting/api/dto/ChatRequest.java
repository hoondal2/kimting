package com.kimting.kimting.api.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String message;
    private String sessionId;  // 선택값 — 대화 세션 식별용 (현재는 로깅에만 사용)
}
