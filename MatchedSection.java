package com.plagiacheck.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchedSection {
    private String originalText;
    private String comparedText;
    private double similarity;
    private int originalPosition;
    private int comparedPosition;
}
