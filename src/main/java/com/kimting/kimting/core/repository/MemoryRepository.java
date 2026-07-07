package com.kimting.kimting.core.repository;

import com.kimting.kimting.core.domain.Memory;
import com.kimting.kimting.core.domain.MemoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MemoryRepository extends JpaRepository<Memory, UUID> {

    List<Memory> findByType(MemoryType type);

    List<Memory> findBySource(String source);

    List<Memory> findByOccurredAtBetweenOrderByOccurredAtDesc(LocalDateTime from, LocalDateTime to);

    List<Memory> findByImportanceGreaterThanEqualOrderByImportanceDesc(Integer importance);

    List<Memory> findByPeopleContainingOrderByOccurredAtDesc(String person);

    List<Memory> findByTagsContaining(String tag);

    List<Memory> findTop20ByOrderByCreatedAtDesc();
}
