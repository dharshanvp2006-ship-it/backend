package com.plagiacheck.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonMetrics {
    private int totalSentencesCompared;
    private int matchedSentences;
    private double averageSimilarity;
    private double maxSimilarity;
    private double minSimilarity;
    private String comparisonMethod;
}
