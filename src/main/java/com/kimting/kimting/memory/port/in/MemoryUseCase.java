package com.kimting.kimting.memory.port.in;

import com.kimting.kimting.memory.domain.Memory;
import com.kimting.kimting.memory.domain.MemoryType;
import com.kimting.kimting.memory.infrastructure.parser.ParseResult;
import com.kimting.kimting.memory.presentation.dto.MemoryUpdateRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MemoryUseCase {

    Memory store(Memory memory);
    Memory storeFromParseResult(ParseResult result, UUID userId);
    List<Memory> search(String query, int topK, UUID userId);
    List<Memory> timeline(LocalDateTime from, LocalDateTime to, UUID userId);
    List<Memory> findByPerson(String person, UUID userId);
    List<Memory> findByType(MemoryType type);
    List<Memory> recent(int limit, UUID userId);
    void strengthen(UUID id);
    void forget(UUID id);
    Memory findById(UUID id);
    Memory update(UUID id, MemoryUpdateRequest req);
    void delete(UUID id);
    List<Memory> list(MemoryType type, String tag, int limit, UUID userId);

    ParseResult parse(String text);
    int importFromStream(InputStream input, UUID userId) throws IOException;
}
