package com.plagiacheck.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlagiarismResult {
    private double similarityScore;
    private String similarityLevel;
    private DocumentInfo originalDocument;
    private DocumentInfo comparedDocument;
    private List<MatchedSection> matchedSections;
    private ComparisonMetrics metrics;
    private String timestamp;
}
