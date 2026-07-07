package com.kimting.kimting.api.dto;

import com.kimting.kimting.core.domain.MemoryType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class MemoryUpdateRequest {
    private MemoryType type;
    private String title;
    private String content;
    private String summary;
    private String emotion;
    private String insight;
    private List<String> tags;
    private Integer importance;
    private Double confidence;
}
