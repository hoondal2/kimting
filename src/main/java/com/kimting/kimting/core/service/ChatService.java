package com.kimting.kimting.core.service;

import com.kimting.kimting.api.dto.ChatRequest;
import com.kimting.kimting.api.dto.ChatResponse;
import com.kimting.kimting.core.domain.Memory;
import com.kimting.kimting.parser.MemoryParser;
import com.kimting.kimting.parser.ParseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final MemoryService memoryService;
    private final MemoryParser memoryParser;

    // API key 없이도 앱이 기동되도록 optional 주입
    @Autowired(required = false)
    private ChatModel chatModel;

    private static final int TOP_K = 5;
    private static final double AUTO_SAVE_CONFIDENCE = 0.6;

    public ChatResponse chat(ChatRequest request) {
        String message = request.getMessage();

        // 1. 관련 기억 검색
        List<Memory> relevantMemories = memoryService.search(message, TOP_K);

        // 2. Claude 호출
        String aiResponse = callClaude(message, relevantMemories);

        // 3. 사용자 메시지에서 Memory 자동 감지·저장
        List<Memory> savedMemories = autoSave(message);

        return ChatResponse.builder()
                .response(aiResponse)
                .usedMemories(relevantMemories)
                .savedMemories(savedMemories)
                .build();
    }

    private String callClaude(String message, List<Memory> memories) {
        if (chatModel == null) {
            throw new IllegalStateException(
                "Chat requires LLM configuration. " +
                "Add spring.ai.openai.api-key to application.yaml " +
                "and set kimting.parser.provider=llm."
            );
        }
        ChatClient chatClient = ChatClient.builder(chatModel).build();
        return chatClient.prompt()
                .system(buildSystemPrompt(memories))
                .user(message)
                .options(OpenAiChatOptions.builder()
                        .temperature(0.7)
                        .maxTokens(2048)
                        .build())
                .call()
                .content();
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

    private List<Memory> autoSave(String message) {
        List<Memory> saved = new ArrayList<>();
        try {
            ParseResult result = memoryParser.parse(message);
            if (shouldSave(result)) {
                Memory memory = memoryService.storeFromParseResult(result);
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
        // 내용이 너무 짧으면 저장하지 않음
        String content = result.getContent();
        return content != null && content.length() >= 5;
    }
}
