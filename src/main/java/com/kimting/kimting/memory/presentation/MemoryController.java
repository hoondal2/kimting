package com.kimting.kimting.memory.presentation;

import com.kimting.kimting.memory.domain.Memory;
import com.kimting.kimting.memory.domain.MemoryType;
import com.kimting.kimting.memory.infrastructure.parser.ParseResult;
import com.kimting.kimting.memory.port.in.MemoryUseCase;
import com.kimting.kimting.memory.presentation.dto.MemoryUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/memories")
@RequiredArgsConstructor
public class MemoryController {

    private final MemoryUseCase memoryUseCase;

    private UUID currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UUID) {
            return (UUID) auth.getPrincipal();
        }
        return null;
    }

    private void checkOwnership(Memory memory) {
        UUID userId = currentUserId();
        if (userId == null || !userId.equals(memory.getUserId())) {
            throw new SecurityException("해당 기억에 대한 접근 권한이 없습니다.");
        }
    }

    @PostMapping
    public ResponseEntity<Memory> store(@RequestBody Memory memory) {
        memory.setUserId(currentUserId());
        return ResponseEntity.ok(memoryUseCase.store(memory));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Memory> getById(@PathVariable UUID id) {
        Memory memory = memoryUseCase.findById(id);
        checkOwnership(memory);
        return ResponseEntity.ok(memory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Memory> update(@PathVariable UUID id,
                                         @RequestBody MemoryUpdateRequest request) {
        checkOwnership(memoryUseCase.findById(id));
        return ResponseEntity.ok(memoryUseCase.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        checkOwnership(memoryUseCase.findById(id));
        memoryUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Memory>> list(
            @RequestParam(required = false) MemoryType type,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(memoryUseCase.list(type, tag, limit, currentUserId()));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Memory>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int topK) {
        if (query.isBlank()) {
            throw new IllegalArgumentException("'query' 파라미터는 비어 있을 수 없습니다.");
        }
        return ResponseEntity.ok(memoryUseCase.search(query, topK, currentUserId()));
    }

    @GetMapping("/timeline")
    public ResponseEntity<List<Memory>> timeline(
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to) {
        return ResponseEntity.ok(memoryUseCase.timeline(from, to, currentUserId()));
    }

    @GetMapping("/people/{person}")
    public ResponseEntity<List<Memory>> byPerson(@PathVariable String person) {
        return ResponseEntity.ok(memoryUseCase.findByPerson(person, currentUserId()));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Memory>> recent() {
        return ResponseEntity.ok(memoryUseCase.recent(20, currentUserId()));
    }

    @PostMapping("/{id}/strengthen")
    public ResponseEntity<Void> strengthen(@PathVariable UUID id) {
        checkOwnership(memoryUseCase.findById(id));
        memoryUseCase.strengthen(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/forget")
    public ResponseEntity<Void> forget(@PathVariable UUID id) {
        checkOwnership(memoryUseCase.findById(id));
        memoryUseCase.forget(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/parse")
    public ResponseEntity<ParseResult> parse(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("'text' field must not be blank.");
        }
        return ResponseEntity.ok(memoryUseCase.parse(text));
    }

    @PostMapping("/parse/confirm")
    public ResponseEntity<Memory> confirm(@RequestBody ParseResult parseResult) {
        return ResponseEntity.ok(memoryUseCase.storeFromParseResult(parseResult, currentUserId()));
    }

    @PostMapping("/import/kakao")
    public ResponseEntity<Map<String, Object>> importKakao(@RequestParam("file") MultipartFile file)
            throws IOException {
        int count = memoryUseCase.importFromStream(file.getInputStream(), currentUserId());
        return ResponseEntity.ok(Map.of(
                "imported", count,
                "message", count + " conversation session(s) saved as Memory."
        ));
    }
}
