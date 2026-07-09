package com.kimting.kimting.api;

import com.kimting.kimting.api.dto.MemoryUpdateRequest;
import com.kimting.kimting.core.domain.Memory;
import com.kimting.kimting.core.domain.MemoryType;
import com.kimting.kimting.core.service.MemoryService;
import com.kimting.kimting.importer.kakao.KakaoImporter;
import com.kimting.kimting.parser.MemoryParser;
import com.kimting.kimting.parser.ParseResult;
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

    private final MemoryService memoryService;
    private final KakaoImporter kakaoImporter;
    private final MemoryParser memoryParser;

    private UUID currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UUID) {
            return (UUID) auth.getPrincipal();
        }
        return null;
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<Memory> store(@RequestBody Memory memory) {
        memory.setUserId(currentUserId());
        return ResponseEntity.ok(memoryService.store(memory));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Memory> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(memoryService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Memory> update(@PathVariable UUID id,
                                         @RequestBody MemoryUpdateRequest request) {
        return ResponseEntity.ok(memoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        memoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── 조회 ─────────────────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<Memory>> list(
            @RequestParam(required = false) MemoryType type,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(memoryService.list(type, tag, limit, currentUserId()));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Memory>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int topK) {
        return ResponseEntity.ok(memoryService.search(query, topK, currentUserId()));
    }

    @GetMapping("/timeline")
    public ResponseEntity<List<Memory>> timeline(
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to) {
        return ResponseEntity.ok(memoryService.timeline(from, to, currentUserId()));
    }

    @GetMapping("/people/{person}")
    public ResponseEntity<List<Memory>> byPerson(@PathVariable String person) {
        return ResponseEntity.ok(memoryService.findByPerson(person, currentUserId()));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Memory>> recent() {
        return ResponseEntity.ok(memoryService.recent(20, currentUserId()));
    }

    // ── 기억 강화/약화 ────────────────────────────────────────────────────────

    @PostMapping("/{id}/strengthen")
    public ResponseEntity<Void> strengthen(@PathVariable UUID id) {
        memoryService.strengthen(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/forget")
    public ResponseEntity<Void> forget(@PathVariable UUID id) {
        memoryService.forget(id);
        return ResponseEntity.ok().build();
    }

    // ── 자연어 파싱 ───────────────────────────────────────────────────────────

    @PostMapping("/parse")
    public ResponseEntity<ParseResult> parse(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("'text' field must not be blank.");
        }
        return ResponseEntity.ok(memoryParser.parse(text));
    }

    @PostMapping("/parse/confirm")
    public ResponseEntity<Memory> confirm(@RequestBody ParseResult parseResult) {
        return ResponseEntity.ok(memoryService.storeFromParseResult(parseResult, currentUserId()));
    }

    // ── 임포터 ────────────────────────────────────────────────────────────────

    @PostMapping("/import/kakao")
    public ResponseEntity<Map<String, Object>> importKakao(@RequestParam("file") MultipartFile file)
            throws IOException {
        UUID userId = currentUserId();
        List<Memory> parsed = kakaoImporter.parse(file.getInputStream());
        parsed.forEach(m -> {
            m.setUserId(userId);
            memoryService.store(m);
        });
        return ResponseEntity.ok(Map.of(
                "imported", parsed.size(),
                "message", parsed.size() + " conversation session(s) saved as Memory."
        ));
    }
}
