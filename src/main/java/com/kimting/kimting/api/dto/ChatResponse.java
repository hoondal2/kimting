package com.kimting.kimting.api.dto;

import com.kimting.kimting.core.domain.Memory;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChatResponse {
    private String response;
    private List<Memory> usedMemories;    // 응답에 활용된 기억
    private List<Memory> savedMemories;   // 이번 메시지에서 새로 저장된 기억
}
