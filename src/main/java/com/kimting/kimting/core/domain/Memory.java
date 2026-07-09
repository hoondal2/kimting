package com.kimting.kimting.core.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "memory")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Memory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemoryType type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "memory_people", joinColumns = @JoinColumn(name = "memory_id"))
    @Column(name = "person")
    @Builder.Default
    private List<String> people = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "memory_tags", joinColumns = @JoinColumn(name = "memory_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    // 1~10, 높을수록 중요
    @Column(nullable = false)
    @Builder.Default
    private Integer importance = 5;

    // 0.0~1.0
    @Column(nullable = false)
    @Builder.Default
    private Double confidence = 1.0;

    private LocalDateTime occurredAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 어떤 사용자의 기억인지 (nullable: 기존 데이터 하위 호환)
    @Column(name = "user_id")
    private UUID userId;

    // 데이터 출처: "kakao", "telegram", "manual" 등
    @Column(nullable = false)
    private String source;

    // 그때 어떤 감정이었는가 ("서운했다", "기뻤다")
    @Column(columnDefinition = "TEXT")
    private String emotion;

    // 돌아보며 깨달은 것 ("내가 관계에서 먼저 연락을 못하는 성격임을 알았다")
    @Column(columnDefinition = "TEXT")
    private String insight;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
