package com.kimting.kimting.core.service;

import com.kimting.kimting.core.domain.Memory;
import com.kimting.kimting.core.domain.MemoryType;
import com.kimting.kimting.core.ranking.MemoryRanking;
import com.kimting.kimting.core.repository.MemoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemoryService {

    private final MemoryRepository memoryRepository;
    private final VectorStore vectorStore;

    @Transactional
    public Memory store(Memory memory) {
        Memory saved = memoryRepository.save(memory);

        String embeddingText = memory.getSummary() != null ? memory.getSummary() : memory.getContent();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("type", saved.getType().name());
        metadata.put("title", saved.getTitle());
        metadata.put("importance", saved.getImportance());
        metadata.put("source", saved.getSource());
        if (saved.getOccurredAt() != null) {
            metadata.put("occurred_at", saved.getOccurredAt().toString());
        }

        Document doc = new Document(saved.getId().toString(), embeddingText, metadata);
        vectorStore.add(List.of(doc));

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Memory> search(String query, int topK) {
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(topK).build()
        );

        List<UUID> ids = docs.stream()
                .map(d -> UUID.fromString(d.getId()))
                .collect(Collectors.toList());

        List<Memory> memories = memoryRepository.findAllById(ids);

        // 벡터 유사도 점수와 메타데이터를 결합해 랭킹 적용
        Map<UUID, Double> scoreMap = new HashMap<>();
        for (int i = 0; i < docs.size(); i++) {
            UUID id = UUID.fromString(docs.get(i).getId());
            double similarity = docs.get(i).getScore() != null ? docs.get(i).getScore() : 0.0;
            scoreMap.put(id, similarity);
        }

        return memories.stream()
                .sorted(Comparator.comparingDouble(m ->
                        -MemoryRanking.score(m, scoreMap.getOrDefault(m.getId(), 0.0))
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Memory> timeline(LocalDateTime from, LocalDateTime to) {
        return memoryRepository.findByOccurredAtBetweenOrderByOccurredAtDesc(from, to);
    }

    @Transactional(readOnly = true)
    public List<Memory> findByPerson(String person) {
        return memoryRepository.findByPeopleContainingOrderByOccurredAtDesc(person);
    }

    @Transactional(readOnly = true)
    public List<Memory> findByType(MemoryType type) {
        return memoryRepository.findByType(type);
    }

    @Transactional(readOnly = true)
    public List<Memory> recent(int limit) {
        return memoryRepository.findTop20ByOrderByCreatedAtDesc();
    }

    @Transactional
    public void strengthen(UUID id) {
        memoryRepository.findById(id).ifPresent(m -> {
            int newImportance = Math.min(10, m.getImportance() + 1);
            m.setImportance(newImportance);
            memoryRepository.save(m);
        });
    }

    @Transactional
    public void forget(UUID id) {
        memoryRepository.findById(id).ifPresent(m -> {
            int newImportance = Math.max(1, m.getImportance() - 1);
            m.setImportance(newImportance);
            memoryRepository.save(m);
        });
    }
}
