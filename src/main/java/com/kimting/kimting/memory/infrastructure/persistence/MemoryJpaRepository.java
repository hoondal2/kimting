package com.kimting.kimting.memory.infrastructure.persistence;

import com.kimting.kimting.memory.domain.Memory;
import com.kimting.kimting.memory.domain.MemoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MemoryJpaRepository extends JpaRepository<Memory, UUID> {

    List<Memory> findByUserId(UUID userId);
    List<Memory> findByUserIdAndType(UUID userId, MemoryType type);
    List<Memory> findTop20ByUserIdOrderByCreatedAtDesc(UUID userId);
    List<Memory> findByUserIdAndOccurredAtBetweenOrderByOccurredAtDesc(UUID userId, LocalDateTime from, LocalDateTime to);
    List<Memory> findByUserIdAndPeopleContainingOrderByOccurredAtDesc(UUID userId, String person);

    List<Memory> findByType(MemoryType type);
    List<Memory> findBySource(String source);
    List<Memory> findByOccurredAtBetweenOrderByOccurredAtDesc(LocalDateTime from, LocalDateTime to);
    List<Memory> findByImportanceGreaterThanEqualOrderByImportanceDesc(Integer importance);
    List<Memory> findByPeopleContainingOrderByOccurredAtDesc(String person);
    List<Memory> findByTagsContaining(String tag);
    List<Memory> findTop20ByOrderByCreatedAtDesc();
}
