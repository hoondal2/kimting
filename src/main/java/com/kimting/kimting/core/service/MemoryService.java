package com.kimting.kimting.core.service;

import com.kimting.kimting.api.dto.MemoryUpdateRequest;
import com.kimting.kimting.core.domain.Memory;
import com.kimting.kimting.core.domain.MemoryType;
import com.kimting.kimting.core.ranking.MemoryRanking;
import com.kimting.kimting.core.repository.MemoryRepository;
import com.kimting.kimting.parser.ParseResult;
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
        String embeddingText = saved.getSummary() != null ? saved.getSummary() : saved.getContent();
        vectorStore.add(List.of(new Document(saved.getId().toString(), embeddingText, buildMetadata(saved))));
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

    @Transactional(readOnly = true)
    public Memory findById(UUID id) {
        return memoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Memory를 찾을 수 없습니다: " + id));
    }

    @Transactional
    public Memory update(UUID id, MemoryUpdateRequest req) {
        Memory memory = findById(id);

        if (req.getType() != null) memory.setType(req.getType());
        if (req.getTitle() != null) memory.setTitle(req.getTitle());
        if (req.getContent() != null) memory.setContent(req.getContent());
        if (req.getSummary() != null) memory.setSummary(req.getSummary());
        if (req.getEmotion() != null) memory.setEmotion(req.getEmotion());
        if (req.getInsight() != null) memory.setInsight(req.getInsight());
        if (req.getTags() != null) memory.setTags(req.getTags());
        if (req.getImportance() != null) memory.setImportance(req.getImportance());
        if (req.getConfidence() != null) memory.setConfidence(req.getConfidence());

        Memory updated = memoryRepository.save(memory);

        // 벡터스토어도 갱신
        vectorStore.delete(List.of(id.toString()));
        String embeddingText = updated.getSummary() != null ? updated.getSummary() : updated.getContent();
        vectorStore.add(List.of(new Document(id.toString(), embeddingText, buildMetadata(updated))));

        return updated;
    }

    @Transactional
    public void delete(UUID id) {
        Memory memory = findById(id);
        vectorStore.delete(List.of(id.toString()));
        memoryRepository.delete(memory);
    }

    @Transactional(readOnly = true)
    public List<Memory> list(MemoryType type, String tag, int limit) {
        List<Memory> base;
        if (type != null) {
            base = memoryRepository.findByType(type);
        } else {
            base = memoryRepository.findAll();
        }
        return base.stream()
                .filter(m -> tag == null || m.getTags().contains(tag))
                .sorted(Comparator.comparing(Memory::getCreatedAt).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Transactional
    public Memory storeFromParseResult(ParseResult result) {
        Memory memory = Memory.builder()
                .type(result.getType())
                .title(result.getTitle())
                .content(result.getContent())
                .summary(result.getSummary())
                .occurredAt(result.getOccurredAt())
                .people(result.getPeople() != null ? result.getPeople() : new ArrayList<>())
                .tags(result.getTags() != null ? result.getTags() : new ArrayList<>())
                .importance(result.getImportance() != null ? result.getImportance() : 5)
                .confidence(result.getConfidence() != null ? result.getConfidence() : 0.8)
                .source(result.getSource() != null ? result.getSource() : "manual")
                .build();
        return store(memory);
    }

    private Map<String, Object> buildMetadata(Memory memory) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("type", memory.getType().name());
        metadata.put("title", memory.getTitle());
        metadata.put("importance", memory.getImportance());
        metadata.put("source", memory.getSource());
        if (memory.getOccurredAt() != null) {
            metadata.put("occurred_at", memory.getOccurredAt().toString());
        }
        return metadata;
    }
}
