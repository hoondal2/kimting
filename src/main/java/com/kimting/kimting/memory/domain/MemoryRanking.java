package com.kimting.kimting.memory.domain;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class MemoryRanking {

    private static final double WEIGHT_SIMILARITY = 0.5;
    private static final double WEIGHT_IMPORTANCE = 0.2;
    private static final double WEIGHT_RECENCY    = 0.2;
    private static final double WEIGHT_CONFIDENCE = 0.1;

    public static double score(Memory memory, double similarity) {
        double importanceScore = memory.getImportance() / 10.0;
        double recencyScore    = recency(memory.getOccurredAt());
        double confidenceScore = memory.getConfidence();

        return (similarity      * WEIGHT_SIMILARITY)
             + (importanceScore * WEIGHT_IMPORTANCE)
             + (recencyScore    * WEIGHT_RECENCY)
             + (confidenceScore * WEIGHT_CONFIDENCE);
    }

    private static double recency(LocalDateTime occurredAt) {
        if (occurredAt == null) return 0.5;
        long daysSince = ChronoUnit.DAYS.between(occurredAt, LocalDateTime.now());
        return Math.exp(-daysSince / 365.0);
    }
}
