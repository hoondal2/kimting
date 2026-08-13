package com.kimting.kimting.memory.infrastructure.persistence;

import com.kimting.kimting.memory.domain.Memory;
import com.kimting.kimting.memory.domain.MemoryType;
import com.kimting.kimting.memory.port.out.MemoryRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MemoryRepositoryAdapter implements MemoryRepositoryPort {

    private final MemoryJpaRepository jpa;

    @Override public Memory save(Memory memory) { return jpa.save(memory); }
    @Override public Optional<Memory> findById(UUID id) { return jpa.findById(id); }
    @Override public List<Memory> findAll() { return jpa.findAll(); }
    @Override public List<Memory> findAllById(List<UUID> ids) { return jpa.findAllById(ids); }
    @Override public void delete(Memory memory) { jpa.delete(memory); }

    @Override public List<Memory> findByUserId(UUID userId) { return jpa.findByUserId(userId); }
    @Override public List<Memory> findByUserIdAndType(UUID userId, MemoryType type) { return jpa.findByUserIdAndType(userId, type); }
    @Override public List<Memory> findTop20ByUserIdOrderByCreatedAtDesc(UUID userId) { return jpa.findTop20ByUserIdOrderByCreatedAtDesc(userId); }
    @Override public List<Memory> findByUserIdAndOccurredAtBetweenOrderByOccurredAtDesc(UUID userId, LocalDateTime from, LocalDateTime to) { return jpa.findByUserIdAndOccurredAtBetweenOrderByOccurredAtDesc(userId, from, to); }
    @Override public List<Memory> findByUserIdAndPeopleContainingOrderByOccurredAtDesc(UUID userId, String person) { return jpa.findByUserIdAndPeopleContainingOrderByOccurredAtDesc(userId, person); }

    @Override public List<Memory> findByType(MemoryType type) { return jpa.findByType(type); }
    @Override public List<Memory> findBySource(String source) { return jpa.findBySource(source); }
    @Override public List<Memory> findByOccurredAtBetweenOrderByOccurredAtDesc(LocalDateTime from, LocalDateTime to) { return jpa.findByOccurredAtBetweenOrderByOccurredAtDesc(from, to); }
    @Override public List<Memory> findByImportanceGreaterThanEqualOrderByImportanceDesc(Integer importance) { return jpa.findByImportanceGreaterThanEqualOrderByImportanceDesc(importance); }
    @Override public List<Memory> findByPeopleContainingOrderByOccurredAtDesc(String person) { return jpa.findByPeopleContainingOrderByOccurredAtDesc(person); }
    @Override public List<Memory> findByTagsContaining(String tag) { return jpa.findByTagsContaining(tag); }
    @Override public List<Memory> findTop20ByOrderByCreatedAtDesc() { return jpa.findTop20ByOrderByCreatedAtDesc(); }
}
