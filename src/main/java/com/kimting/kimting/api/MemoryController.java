package com.kimting.kimting.api;

import com.kimting.kimting.core.domain.Memory;
import com.kimting.kimting.core.service.MemoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/memories")
@RequiredArgsConstructor
public class MemoryController {

    private final MemoryService memoryService;

    @PostMapping
    public ResponseEntity<Memory> store(@RequestBody Memory memory) {
        return ResponseEntity.ok(memoryService.store(memory));
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
}
