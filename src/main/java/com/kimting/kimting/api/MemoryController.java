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

    // ── CRUD ─────────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<Memory> store(@RequestBody Memory memory) {
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

    /** type, tag 필터 + limit. 예) GET /api/memories?type=SCHEDULE&limit=10 */
    @GetMapping
    public ResponseEntity<List<Memory>> list(
            @RequestParam(required = false) MemoryType type,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(memoryService.list(type, tag, limit));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Memory>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int topK) {
        return ResponseEntity.ok(memoryService.search(query, topK));
    }

    @GetMapping("/timeline")
    public ResponseEntity<List<Memory>> timeline(
            @RequestParam LocalDateTime from,
            @RequestParam LocalDateTime to) {
        return ResponseEntity.ok(memoryService.timeline(from, to));
    }

    @GetMapping("/people/{person}")
    public ResponseEntity<List<Memory>> byPerson(@PathVariable String person) {
        return ResponseEntity.ok(memoryService.findByPerson(person));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Memory>> recent() {
        return ResponseEntity.ok(memoryService.recent(20));
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

    /**
     * 자연어 텍스트를 파싱해 Memory 구조를 제안한다. 저장하지 않는다.
     * 예) POST /api/memories/parse  { "text": "나 내일 10시에 회의있음" }
     */
    @PostMapping("/parse")
    public ResponseEntity<ParseResult> parse(@RequestBody Map<String, String> body) {
        String text = body.get("text");
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text 필드가 비어있습니다.");
        }
        return ResponseEntity.ok(memoryParser.parse(text));
    }

    /**
     * /parse 응답을 검토 후 그대로 보내면 Memory로 저장된다.
     * 클라이언트가 필요에 따라 응답 값을 수정한 뒤 전송할 수 있다.
     */
    @PostMapping("/parse/confirm")
    public ResponseEntity<Memory> confirm(@RequestBody ParseResult parseResult) {
        return ResponseEntity.ok(memoryService.storeFromParseResult(parseResult));
    }

    // ── 임포터 ────────────────────────────────────────────────────────────────

    @PostMapping("/import/kakao")
    public ResponseEntity<Map<String, Object>> importKakao(@RequestParam("file") MultipartFile file)
            throws IOException {
        List<Memory> parsed = kakaoImporter.parse(file.getInputStream());
        parsed.forEach(memoryService::store);
        return ResponseEntity.ok(Map.of(
                "imported", parsed.size(),
                "message", parsed.size() + "개의 대화 세션을 Memory로 저장했습니다."
        ));
    }
}
