package com.kimting.kimting.memory.application;

import com.kimting.kimting.memory.domain.Memory;
import com.kimting.kimting.memory.infrastructure.parser.MemoryParser;
import com.kimting.kimting.memory.infrastructure.parser.ParseResult;
import com.kimting.kimting.memory.port.in.ChatUseCase;
import com.kimting.kimting.memory.port.in.MemoryUseCase;
import com.kimting.kimting.memory.port.out.LlmPort;
import com.kimting.kimting.memory.presentation.dto.ChatRequest;
import com.kimting.kimting.memory.presentation.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService implements ChatUseCase {

    private final MemoryUseCase memoryUseCase;
    private final MemoryParser memoryParser;
    private final LlmPort llmPort;

    private static final int TOP_K = 5;
    private static final double AUTO_SAVE_CONFIDENCE = 0.6;

    @Override
    public ChatResponse chat(ChatRequest request, UUID userId) {
        String message = request.getMessage();

        List<Memory> relevantMemories = memoryUseCase.search(message, TOP_K, userId);
        String aiResponse = llmPort.chat(buildSystemPrompt(relevantMemories), message);
        List<Memory> savedMemories = autoSave(message, userId);

        return ChatResponse.builder()
                .response(aiResponse)
                .usedMemories(relevantMemories)
                .savedMemories(savedMemories)
                .build();
    }

    private String buildSystemPrompt(List<Memory> memories) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 M월 d일"));

        StringBuilder sb = new StringBuilder();
        sb.append("당신은 kimting의 AI 비서입니다. 사용자의 장기 기억을 바탕으로 개인화된 대화를 합니다.\n");
        sb.append("오늘 날짜: ").append(today).append("\n\n");

        if (!memories.isEmpty()) {
            sb.append("[사용자의 관련 기억]\n");
            for (int i = 0; i < memories.size(); i++) {
                Memory m = memories.get(i);
                sb.append(i + 1).append(". [").append(m.getType()).append("] ");
                sb.append(m.getTitle());
                if (m.getOccurredAt() != null) {
                    sb.append(" | ").append(m.getOccurredAt().format(DateTimeFormatter.ofPattern("M월 d일 HH:mm")));
                }
                sb.append(" | 중요도: ").append(m.getImportance());
                if (m.getContent() != null && !m.getContent().equals(m.getTitle())) {
                    sb.append("\n   ").append(m.getContent());
                }
                sb.append("\n");
            }
            sb.append("\n");
        }

        sb.append("규칙:\n");
        sb.append("- 위 기억을 자연스럽게 대화에 활용하세요\n");
        sb.append("- 기억에 없는 사실은 지어내지 마세요\n");
        sb.append("- 친근하고 자연스러운 한국어로 대화하세요\n");
        sb.append("- 사용자가 새로운 정보(일정, 할 일, 감정 등)를 말하면 간단히 확인해주세요\n");

        return sb.toString();
    }

    private List<Memory> autoSave(String message, UUID userId) {
        List<Memory> saved = new ArrayList<>();
        try {
            ParseResult result = memoryParser.parse(message);
            if (shouldSave(result)) {
                Memory memory = memoryUseCase.storeFromParseResult(result, userId);
                saved.add(memory);
                log.info("채팅 메시지에서 Memory 자동 저장: type={}, title={}", result.getType(), result.getTitle());
            }
        } catch (Exception e) {
            log.warn("Memory 자동 저장 실패: {}", e.getMessage());
        }
        return saved;
    }

    private boolean shouldSave(ParseResult result) {
        if (result == null || result.getType() == null) return false;
        if (result.getConfidence() == null || result.getConfidence() < AUTO_SAVE_CONFIDENCE) return false;
        String content = result.getContent();
        return content != null && content.length() >= 5;
    }
}
