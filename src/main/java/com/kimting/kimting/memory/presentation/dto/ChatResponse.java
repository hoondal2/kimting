package com.kimting.kimting.memory.presentation.dto;

import com.kimting.kimting.memory.domain.Memory;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChatResponse {
    private String response;
    private List<Memory> usedMemories;
    private List<Memory> savedMemories;
}
