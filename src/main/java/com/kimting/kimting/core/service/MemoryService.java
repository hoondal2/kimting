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
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
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
    public List<Memory> search(String query, int topK, UUID userId) {
        SearchRequest.Builder builder = SearchRequest.builder().query(query).topK(topK);
        if (userId != null) {
            FilterExpressionBuilder b = new FilterExpressionBuilder();
            builder.filterExpression(b.eq("user_id", userId.toString()).build());
        }

        List<Document> docs = vectorStore.similaritySearch(builder.build());

        List<UUID> ids = docs.stream()
                .map(d -> UUID.fromString(d.getId()))
                .collect(Collectors.toList());

        List<Memory> memories = memoryRepository.findAllById(ids);

        Map<UUID, Double> scoreMap = new HashMap<>();
        for (Document doc : docs) {
            UUID id = UUID.fromString(doc.getId());
            double similarity = doc.getScore() != null ? doc.getScore() : 0.0;
            scoreMap.put(id, similarity);
        }

        return memories.stream()
                .sorted(Comparator.comparingDouble(m ->
                        -MemoryRanking.score(m, scoreMap.getOrDefault(m.getId(), 0.0))
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Memory> timeline(LocalDateTime from, LocalDateTime to, UUID userId) {
        if (userId != null) {
            return memoryRepository.findByUserIdAndOccurredAtBetweenOrderByOccurredAtDesc(userId, from, to);
        }
        return memoryRepository.findByOccurredAtBetweenOrderByOccurredAtDesc(from, to);
    }

    @Transactional(readOnly = true)
    public List<Memory> findByPerson(String person, UUID userId) {
        if (userId != null) {
            return memoryRepository.findByUserIdAndPeopleContainingOrderByOccurredAtDesc(userId, person);
        }
        return memoryRepository.findByPeopleContainingOrderByOccurredAtDesc(person);
    }

    @Transactional(readOnly = true)
    public List<Memory> findByType(MemoryType type) {
        return memoryRepository.findByType(type);
    }

    @Transactional(readOnly = true)
    public List<Memory> recent(int limit, UUID userId) {
        if (userId != null) {
            return memoryRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId);
        }
        return memoryRepository.findTop20ByOrderByCreatedAtDesc();
    }

    @Transactional
    public void strengthen(UUID id) {
        memoryRepository.findById(id).ifPresent(m -> {
            m.setImportance(Math.min(10, m.getImportance() + 1));
            memoryRepository.save(m);
        });
    }

    @Transactional
    public void forget(UUID id) {
        memoryRepository.findById(id).ifPresent(m -> {
            m.setImportance(Math.max(1, m.getImportance() - 1));
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
    public List<Memory> list(MemoryType type, String tag, int limit, UUID userId) {
        List<Memory> base;
        if (userId != null) {
            base = (type != null)
                    ? memoryRepository.findByUserIdAndType(userId, type)
                    : memoryRepository.findByUserId(userId);
        } else {
            base = (type != null) ? memoryRepository.findByType(type) : memoryRepository.findAll();
        }
        return base.stream()
                .filter(m -> tag == null || m.getTags().contains(tag))
                .sorted(Comparator.comparing(Memory::getCreatedAt).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Transactional
    public Memory storeFromParseResult(ParseResult result, UUID userId) {
        Memory memory = Memory.builder()
                .userId(userId)
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
        if (memory.getUserId() != null) {
            metadata.put("user_id", memory.getUserId().toString());
        }
        if (memory.getOccurredAt() != null) {
            metadata.put("occurred_at", memory.getOccurredAt().toString());
        }
        return metadata;
    }
}
