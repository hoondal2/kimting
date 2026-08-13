package com.kimting.kimting.memory.infrastructure.parser;

import com.kimting.kimting.memory.domain.MemoryType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class LlmMemoryParser implements MemoryParser {

    private final ChatClient chatClient;
    private final RuleBasedMemoryParser fallback = new RuleBasedMemoryParser();

    public LlmMemoryParser(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    @Override
    public ParseResult parse(String text) {
        try {
            LlmParseOutput output = chatClient.prompt()
                    .system(buildSystemPrompt())
                    .user(text)
                    .call()
                    .entity(LlmParseOutput.class);

            return convert(output, text);
        } catch (Exception e) {
            log.warn("LLM 파싱 실패, 규칙 기반으로 전환: {}", e.getMessage());
            return fallback.parse(text);
        }
    }

    private String buildSystemPrompt() {
        LocalDate today = LocalDate.now();
        String dayOfWeek = switch (today.getDayOfWeek()) {
            case MONDAY -> "월요일";
            case TUESDAY -> "화요일";
            case WEDNESDAY -> "수요일";
            case THURSDAY -> "목요일";
            case FRIDAY -> "금요일";
            case SATURDAY -> "토요일";
            case SUNDAY -> "일요일";
        };
        String todayStr = "%d년 %d월 %d일 (%s)".formatted(
                today.getYear(), today.getMonthValue(), today.getDayOfMonth(), dayOfWeek);

        return """
                당신은 개인 기억 파싱 전문가입니다.
                사용자의 한국어 입력을 분석해 구조화된 기억 데이터로 변환합니다.

                오늘: %s
                "내일"은 오늘 +1일, "모레"는 +2일, "어제"는 -1일로 계산하세요.

                타입 선택 기준:
                SCHEDULE    - 약속, 회의, 일정, 예약, 모임
                TODO        - 할 일, 사야 할 것, 해야 하는 것
                PREFERENCE  - 취향, 선호도, 좋아하는 것/싫어하는 것
                RELATIONSHIP - 특정 인물에 대한 정보
                EVENT       - 발생한 사건, 경험
                FACT        - 사실 정보, 지식
                EMOTION     - 감정 기록, 기분
                REFLECTION  - 성찰, 깨달음, 배운 점

                importance (1~10):
                9~10 - 절대 잊으면 안 되는 중요 약속
                7~8  - 중요 일정/핵심 인물 정보
                5~6  - 일반 메모/일상 기록
                3~4  - 가벼운 취향/사소한 사실
                1~2  - 매우 사소한 정보

                JSON만 반환하세요 (다른 텍스트 없이):
                {
                  "type": "SCHEDULE",
                  "title": "간결한 제목 (20자 이내)",
                  "content": "내용",
                  "occurredAt": "yyyy-MM-dd'T'HH:mm:ss 또는 null 문자열",
                  "people": ["관련 인물"],
                  "tags": ["태그"],
                  "importance": 5,
                  "confidence": 0.9
                }
                """.formatted(todayStr);
    }

    private ParseResult convert(LlmParseOutput output, String originalText) {
        MemoryType type;
        try {
            type = MemoryType.valueOf(output.getType());
        } catch (Exception e) {
            type = MemoryType.FACT;
        }

        LocalDateTime occurredAt = null;
        if (output.getOccurredAt() != null && !output.getOccurredAt().equals("null")) {
            try {
                occurredAt = LocalDateTime.parse(output.getOccurredAt());
            } catch (Exception e) {
                log.debug("occurredAt 파싱 실패: {}", output.getOccurredAt());
            }
        }

        return ParseResult.builder()
                .type(type)
                .title(output.getTitle())
                .content(output.getContent() != null ? output.getContent() : originalText)
                .occurredAt(occurredAt)
                .people(output.getPeople() != null ? output.getPeople() : new ArrayList<>())
                .tags(output.getTags() != null ? output.getTags() : new ArrayList<>())
                .importance(output.getImportance() != null ? output.getImportance() : 5)
                .confidence(output.getConfidence() != null ? output.getConfidence() : 0.8)
                .originalText(originalText)
                .source("manual")
                .build();
    }

    @Data
    @NoArgsConstructor
    static class LlmParseOutput {
        private String type;
        private String title;
        private String content;
        private String occurredAt;
        private List<String> people;
        private List<String> tags;
        private Integer importance;
        private Double confidence;
    }
}
