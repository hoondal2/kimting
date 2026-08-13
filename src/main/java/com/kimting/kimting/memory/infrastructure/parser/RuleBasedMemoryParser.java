package com.kimting.kimting.memory.infrastructure.parser;

import com.kimting.kimting.memory.domain.MemoryType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RuleBasedMemoryParser implements MemoryParser {

    private static final Pattern AM_PM_TIME =
            Pattern.compile("(오전|오후)\\s*(\\d{1,2})시(?:\\s*(\\d{1,2})분)?");

    private static final Pattern PLAIN_TIME =
            Pattern.compile("(?<!전\\s)(?<!후\\s)(\\d{1,2})시(?:\\s*(\\d{1,2})분)?");

    private static final Pattern MONTH_DAY =
            Pattern.compile("(\\d{1,2})월\\s*(\\d{1,2})일");

    @Override
    public ParseResult parse(String text) {
        MemoryType type = detectType(text);
        LocalDate date = detectDate(text);
        LocalTime time = detectTime(text);

        LocalDateTime occurredAt = null;
        if (date != null && time != null) {
            occurredAt = LocalDateTime.of(date, time);
        } else if (date != null) {
            occurredAt = date.atStartOfDay();
        } else if (time != null) {
            occurredAt = LocalDateTime.of(LocalDate.now(), time);
        }

        String title = text.length() > 20 ? text.substring(0, 20) + "..." : text;

        return ParseResult.builder()
                .type(type)
                .title(title)
                .content(text)
                .occurredAt(occurredAt)
                .people(new ArrayList<>())
                .tags(detectTags(text, type))
                .importance(detectImportance(type))
                .confidence(0.6)
                .originalText(text)
                .source("manual")
                .build();
    }

    private MemoryType detectType(String text) {
        if (containsAny(text, "회의", "약속", "모임", "미팅", "예약", "일정", "있음", "있어", "갈게", "만나"))
            return MemoryType.SCHEDULE;
        if (containsAny(text, "해야", "사야", "할 일", "해야지", "잊지", "기억해", "사러"))
            return MemoryType.TODO;
        if (containsAny(text, "힘들었", "기뻤", "슬펐", "화났", "행복했", "기분", "느꼈", "외로웠", "설렜"))
            return MemoryType.EMOTION;
        if (containsAny(text, "깨달았", "알게 됐", "알았다", "생각해보니", "돌아보니", "배웠다", "느꼈다"))
            return MemoryType.REFLECTION;
        if (containsAny(text, "좋아한다", "싫어한다", "선호", "취향", "좋아해", "싫어해", "즐겨"))
            return MemoryType.PREFERENCE;
        if (containsAny(text, "이라고", "인 것 같", "사람이", "친구", "누나", "형", "동생", "부장", "팀장"))
            return MemoryType.RELATIONSHIP;
        return MemoryType.FACT;
    }

    private LocalDate detectDate(String text) {
        LocalDate today = LocalDate.now();
        if (text.contains("오늘")) return today;
        if (text.contains("내일")) return today.plusDays(1);
        if (text.contains("모레")) return today.plusDays(2);
        if (text.contains("글피")) return today.plusDays(3);
        if (text.contains("어제")) return today.minusDays(1);
        if (text.contains("그제")) return today.minusDays(2);

        Matcher m = MONTH_DAY.matcher(text);
        if (m.find()) {
            int month = Integer.parseInt(m.group(1));
            int day = Integer.parseInt(m.group(2));
            return LocalDate.of(today.getYear(), month, day);
        }
        return null;
    }

    private LocalTime detectTime(String text) {
        Matcher ampm = AM_PM_TIME.matcher(text);
        if (ampm.find()) {
            int hour = Integer.parseInt(ampm.group(2));
            int minute = ampm.group(3) != null ? Integer.parseInt(ampm.group(3)) : 0;
            if (ampm.group(1).equals("오후") && hour != 12) hour += 12;
            if (ampm.group(1).equals("오전") && hour == 12) hour = 0;
            return LocalTime.of(hour, minute);
        }

        Matcher plain = PLAIN_TIME.matcher(text);
        if (plain.find()) {
            int hour = Integer.parseInt(plain.group(1));
            int minute = plain.group(2) != null ? Integer.parseInt(plain.group(2)) : 0;
            if (hour >= 1 && hour <= 23) {
                return LocalTime.of(hour, minute);
            }
        }
        return null;
    }

    private List<String> detectTags(String text, MemoryType type) {
        List<String> tags = new ArrayList<>();
        if (containsAny(text, "회의", "미팅")) tags.add("회의");
        if (containsAny(text, "약속", "만나")) tags.add("약속");
        if (containsAny(text, "밥", "점심", "저녁", "식사")) tags.add("식사");
        if (containsAny(text, "운동", "헬스", "달리기")) tags.add("운동");
        return tags;
    }

    private int detectImportance(MemoryType type) {
        return switch (type) {
            case SCHEDULE -> 7;
            case TODO -> 6;
            case RELATIONSHIP -> 7;
            case EMOTION, REFLECTION -> 6;
            default -> 5;
        };
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }
}
