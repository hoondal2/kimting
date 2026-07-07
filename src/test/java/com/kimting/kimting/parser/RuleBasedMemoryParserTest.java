package com.kimting.kimting.parser;

import com.kimting.kimting.core.domain.MemoryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RuleBasedMemoryParserTest {

    private RuleBasedMemoryParser parser;

    @BeforeEach
    void setUp() {
        parser = new RuleBasedMemoryParser();
    }

    @Test
    @DisplayName("회의 키워드 → SCHEDULE 타입")
    void detectScheduleType() {
        ParseResult result = parser.parse("내일 오전 10시에 팀 회의있음");

        assertThat(result.getType()).isEqualTo(MemoryType.SCHEDULE);
        assertThat(result.getImportance()).isEqualTo(7);
        assertThat(result.getSource()).isEqualTo("manual");
    }

    @Test
    @DisplayName("할 일 키워드 → TODO 타입")
    void detectTodoType() {
        ParseResult result = parser.parse("우유 사야됨");

        assertThat(result.getType()).isEqualTo(MemoryType.TODO);
        assertThat(result.getContent()).isEqualTo("우유 사야됨");
    }

    @Test
    @DisplayName("감정 키워드 → EMOTION 타입")
    void detectEmotionType() {
        ParseResult result = parser.parse("오늘 진짜 힘들었다");

        assertThat(result.getType()).isEqualTo(MemoryType.EMOTION);
    }

    @Test
    @DisplayName("성찰 키워드 → REFLECTION 타입")
    void detectReflectionType() {
        ParseResult result = parser.parse("생각해보니 내가 너무 성급하게 결정했던 것 같다");

        assertThat(result.getType()).isEqualTo(MemoryType.REFLECTION);
    }

    @Test
    @DisplayName("오전 시간 파싱 → 올바른 hour/minute")
    void parseMorningTime() {
        ParseResult result = parser.parse("내일 오전 10시에 회의있음");

        assertThat(result.getOccurredAt()).isNotNull();
        assertThat(result.getOccurredAt().getHour()).isEqualTo(10);
        assertThat(result.getOccurredAt().getMinute()).isEqualTo(0);
    }

    @Test
    @DisplayName("오후 시간 파싱 → 12시간 → 24시간 변환")
    void parseAfternoonTime() {
        ParseResult result = parser.parse("오후 3시 30분에 약속");

        assertThat(result.getOccurredAt()).isNotNull();
        assertThat(result.getOccurredAt().getHour()).isEqualTo(15);
        assertThat(result.getOccurredAt().getMinute()).isEqualTo(30);
    }

    @Test
    @DisplayName("'내일' → 오늘 +1일로 파싱")
    void parseTomorrow() {
        ParseResult result = parser.parse("내일 회의있음");

        LocalDate expectedDate = LocalDate.now().plusDays(1);
        assertThat(result.getOccurredAt()).isNotNull();
        assertThat(result.getOccurredAt().toLocalDate()).isEqualTo(expectedDate);
    }

    @Test
    @DisplayName("'오늘' → 오늘 날짜로 파싱")
    void parseToday() {
        ParseResult result = parser.parse("오늘 오후 2시에 미팅");

        assertThat(result.getOccurredAt().toLocalDate()).isEqualTo(LocalDate.now());
        assertThat(result.getOccurredAt().getHour()).isEqualTo(14);
    }

    @Test
    @DisplayName("날짜/시간 없으면 occurredAt은 null")
    void noDateTimeReturnsNull() {
        ParseResult result = parser.parse("우유 사야됨");

        assertThat(result.getOccurredAt()).isNull();
    }

    @Test
    @DisplayName("신뢰도는 0.6으로 고정 (규칙 기반)")
    void confidenceIsLow() {
        ParseResult result = parser.parse("내일 약속있음");

        assertThat(result.getConfidence()).isEqualTo(0.6);
    }
}
