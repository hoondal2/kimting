package com.kimting.kimting.mcp;

import com.kimting.kimting.core.domain.Memory;
import com.kimting.kimting.core.service.MemoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MemoryMcpTool {

    private final MemoryService memoryService;

    @Tool(description = "사용자의 기억에서 질문과 관련된 내용을 검색한다. 과거 대화, 약속, 일정, 선호도 등을 찾을 때 사용한다.")
    public String searchMemory(String query) {
        List<Memory> results = memoryService.search(query, 5, null);
        if (results.isEmpty()) {
            return "관련된 기억을 찾지 못했다.";
        }
        return results.stream()
                .map(m -> "[%s] %s\n%s".formatted(m.getType(), m.getTitle(), m.getSummary() != null ? m.getSummary() : m.getContent()))
                .collect(Collectors.joining("\n\n"));
    }

    @Tool(description = "최근에 저장된 기억 목록을 반환한다.")
    public String recentMemories() {
        List<Memory> results = memoryService.recent(10, null);
        if (results.isEmpty()) {
            return "저장된 기억이 없다.";
        }
        return results.stream()
                .map(m -> "[%s] %s (%s)".formatted(m.getType(), m.getTitle(), m.getSource()))
                .collect(Collectors.joining("\n"));
    }

    @Tool(description = "특정 사람과 관련된 기억을 검색한다.")
    public String memoriesByPerson(String personName) {
        List<Memory> results = memoryService.findByPerson(personName, null);
        if (results.isEmpty()) {
            return personName + "과(와) 관련된 기억을 찾지 못했다.";
        }
        return results.stream()
                .map(m -> "[%s] %s".formatted(m.getType(), m.getTitle()))
                .collect(Collectors.joining("\n"));
    }
}
