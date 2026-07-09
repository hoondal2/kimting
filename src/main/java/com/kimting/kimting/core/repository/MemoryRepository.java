package com.kimting.kimting.core.repository;

import com.kimting.kimting.core.domain.Memory;
import com.kimting.kimting.core.domain.MemoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MemoryRepository extends JpaRepository<Memory, UUID> {

    // userId 격리 쿼리
    List<Memory> findByUserId(UUID userId);
    List<Memory> findByUserIdAndType(UUID userId, MemoryType type);
    List<Memory> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<Memory> findTop20ByUserIdOrderByCreatedAtDesc(UUID userId);
    List<Memory> findByUserIdAndOccurredAtBetweenOrderByOccurredAtDesc(UUID userId, LocalDateTime from, LocalDateTime to);
    List<Memory> findByUserIdAndPeopleContainingOrderByOccurredAtDesc(UUID userId, String person);

    // 하위 호환 (userId 없는 기존 데이터용)
    List<Memory> findByType(MemoryType type);
    List<Memory> findBySource(String source);
    List<Memory> findByOccurredAtBetweenOrderByOccurredAtDesc(LocalDateTime from, LocalDateTime to);
    List<Memory> findByImportanceGreaterThanEqualOrderByImportanceDesc(Integer importance);
    List<Memory> findByPeopleContainingOrderByOccurredAtDesc(String person);
    List<Memory> findByTagsContaining(String tag);
    List<Memory> findTop20ByOrderByCreatedAtDesc();
}
