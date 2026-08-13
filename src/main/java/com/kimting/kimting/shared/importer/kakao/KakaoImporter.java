package com.kimting.kimting.shared.importer.kakao;

import com.kimting.kimting.memory.domain.Memory;
import com.kimting.kimting.memory.domain.MemoryType;
import com.kimting.kimting.shared.importer.MemoryImporter;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

@Component
public class KakaoImporter implements MemoryImporter {

    private static final Pattern ANDROID_MSG =
        Pattern.compile("^\\[(.+?)\\] \\[(오전|오후) (\\d{1,2}:\\d{2})\\] (.+)$");

    private static final Pattern IOS_MSG =
        Pattern.compile("^(오전|오후) (\\d{1,2}:\\d{2}), (.+?) : (.+)$");

    private static final Pattern DATE_LINE =
        Pattern.compile("^(\\d{4})년 (\\d{1,2})월 (\\d{1,2})일");

    private static final Pattern SYSTEM_MSG =
        Pattern.compile(".+(님이 들어왔습니다|님이 나갔습니다|님을 초대했습니다)");

    private static final int SESSION_GAP_MINUTES = 30;

    @Override
    public String getSource() {
        return "kakao";
    }

    @Override
    public List<Memory> parse(InputStream input) throws IOException {
        List<String> lines = new BufferedReader(
            new InputStreamReader(input, StandardCharsets.UTF_8)
        ).lines().collect(Collectors.toList());

        Format format = detectFormat(lines);
        List<KakaoMessage> messages = parseMessages(lines, format);
        return groupIntoMemories(messages);
    }

    private Format detectFormat(List<String> lines) {
        for (String line : lines) {
            if (ANDROID_MSG.matcher(line).matches()) return Format.ANDROID;
            if (IOS_MSG.matcher(line).matches()) return Format.IOS;
        }
        return Format.ANDROID;
    }

    private List<KakaoMessage> parseMessages(List<String> lines, Format format) {
        List<KakaoMessage> messages = new ArrayList<>();
        LocalDate currentDate = null;

        for (String line : lines) {
            Matcher dateMatcher = DATE_LINE.matcher(line);
            if (dateMatcher.find()) {
                currentDate = LocalDate.of(
                    Integer.parseInt(dateMatcher.group(1)),
                    Integer.parseInt(dateMatcher.group(2)),
                    Integer.parseInt(dateMatcher.group(3))
                );
                continue;
            }

            if (currentDate == null) continue;
            if (line.isBlank()) continue;
            if (SYSTEM_MSG.matcher(line).matches()) continue;

            KakaoMessage msg = (format == Format.ANDROID)
                ? parseAndroid(line, currentDate)
                : parseIos(line, currentDate);

            if (msg != null) {
                messages.add(msg);
            } else if (!messages.isEmpty()) {
                KakaoMessage last = messages.get(messages.size() - 1);
                last.appendContent(line);
            }
        }

        return messages;
    }

    private KakaoMessage parseAndroid(String line, LocalDate date) {
        Matcher m = ANDROID_MSG.matcher(line);
        if (!m.matches()) return null;
        return new KakaoMessage(
            LocalDateTime.of(date, parseTime(m.group(2), m.group(3))),
            m.group(1),
            m.group(4)
        );
    }

    private KakaoMessage parseIos(String line, LocalDate date) {
        Matcher m = IOS_MSG.matcher(line);
        if (!m.matches()) return null;
        return new KakaoMessage(
            LocalDateTime.of(date, parseTime(m.group(1), m.group(2))),
            m.group(3),
            m.group(4)
        );
    }

    private LocalTime parseTime(String ampm, String timeStr) {
        String[] parts = timeStr.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        if (ampm.equals("오후") && hour != 12) hour += 12;
        if (ampm.equals("오전") && hour == 12) hour = 0;

        return LocalTime.of(hour, minute);
    }

    private List<Memory> groupIntoMemories(List<KakaoMessage> messages) {
        if (messages.isEmpty()) return Collections.emptyList();

        List<Memory> memories = new ArrayList<>();
        List<KakaoMessage> session = new ArrayList<>();
        session.add(messages.get(0));

        for (int i = 1; i < messages.size(); i++) {
            long gapMinutes = java.time.Duration.between(
                messages.get(i - 1).dateTime,
                messages.get(i).dateTime
            ).toMinutes();

            if (gapMinutes > SESSION_GAP_MINUTES) {
                memories.add(buildMemory(session));
                session = new ArrayList<>();
            }
            session.add(messages.get(i));
        }
        memories.add(buildMemory(session));

        return memories;
    }

    private Memory buildMemory(List<KakaoMessage> session) {
        LocalDateTime firstTime = session.get(0).dateTime;

        List<String> people = session.stream()
            .map(m -> m.sender)
            .distinct()
            .collect(Collectors.toList());

        StringBuilder content = new StringBuilder();
        for (KakaoMessage msg : session) {
            content.append("[").append(msg.sender).append("] ").append(msg.getContent()).append("\n");
        }

        String participantStr = people.size() <= 3
            ? String.join(", ", people)
            : people.get(0) + " 외 " + (people.size() - 1) + "명";
        String title = participantStr + "과의 대화 (" + firstTime.toLocalDate() + ")";

        String fullContent = content.toString().trim();
        String summary = fullContent.length() > 500 ? fullContent.substring(0, 500) + "..." : null;

        return Memory.builder()
            .type(MemoryType.CONVERSATION)
            .title(title)
            .content(fullContent)
            .summary(summary)
            .people(people)
            .occurredAt(firstTime)
            .source("kakao")
            .importance(5)
            .confidence(0.8)
            .build();
    }

    private enum Format { ANDROID, IOS }

    private static class KakaoMessage {
        final LocalDateTime dateTime;
        final String sender;
        private final StringBuilder content;

        KakaoMessage(LocalDateTime dateTime, String sender, String content) {
            this.dateTime = dateTime;
            this.sender = sender;
            this.content = new StringBuilder(content);
        }

        void appendContent(String line) {
            content.append("\n").append(line);
        }

        String getContent() {
            return content.toString();
        }
    }
}
