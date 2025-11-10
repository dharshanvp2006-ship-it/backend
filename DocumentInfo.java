package com.plagiacheck.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentInfo {
    private String fileName;
    private long fileSize;
    private int wordCount;
    private int sentenceCount;
    private int paragraphCount;
}
