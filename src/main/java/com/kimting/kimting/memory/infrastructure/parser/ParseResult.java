package com.kimting.kimting.memory.infrastructure.parser;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kimting.kimting.memory.domain.MemoryType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParseResult {

    private MemoryType type;
    private String title;
    private String content;
    private String summary;
    private LocalDateTime occurredAt;

    @Builder.Default
    private List<String> people = new ArrayList<>();

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    private Integer importance;
    private Double confidence;

    private String originalText;

    @Builder.Default
    private String source = "manual";
}
