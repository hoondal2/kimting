package com.kimting.kimting.parser;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kimting.kimting.core.domain.MemoryType;
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

    /** 사용자가 입력한 원문 */
    private String originalText;

    /** 저장 시 source 값 (기본값 "manual") */
    @Builder.Default
    private String source = "manual";
}
