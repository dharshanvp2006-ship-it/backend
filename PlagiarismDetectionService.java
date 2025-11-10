package com.plagiacheck.service;

import com.plagiacheck.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlagiarismDetectionService {

    private final PdfExtractionService pdfExtractionService;
    private final TextProcessingService textProcessingService;
    private final SimilarityCalculationService similarityCalculationService;

    public PlagiarismResult detectPlagiarism(MultipartFile originalFile, MultipartFile comparedFile) throws IOException {
        log.info("Starting plagiarism detection between {} and {}", 
                originalFile.getOriginalFilename(), comparedFile.getOriginalFilename());

        // Validate files
        pdfExtractionService.validatePdfFile(originalFile);
        pdfExtractionService.validatePdfFile(comparedFile);

        // Extract text from both PDFs
        String originalText = pdfExtractionService.extractTextFromPdf(originalFile);
        String comparedText = pdfExtractionService.extractTextFromPdf(comparedFile);

        // Clean and process text
        String cleanedOriginal = textProcessingService.cleanText(originalText);
        String cleanedCompared = textProcessingService.cleanText(comparedText);

        // Get document information
        DocumentInfo originalInfo = pdfExtractionService.getDocumentInfo(originalFile, cleanedOriginal);
        DocumentInfo comparedInfo = pdfExtractionService.getDocumentInfo(comparedFile, cleanedCompared);

        // Calculate overall similarity using multiple methods
        double cosineSimilarity = similarityCalculationService.calculateCosineSimilarity(cleanedOriginal, cleanedCompared);
        double jaccardSimilarity = similarityCalculationService.calculateJaccardSimilarity(cleanedOriginal, cleanedCompared);
        
        // Weighted average for final score
        double overallSimilarity = (cosineSimilarity * 0.6 + jaccardSimilarity * 0.4) * 100;

        // Find matched sections at sentence level
        List<String> originalSentences = textProcessingService.splitIntoSentences(cleanedOriginal);
        List<String> comparedSentences = textProcessingService.splitIntoSentences(cleanedCompared);
        
        List<MatchedSection> matchedSections = findMatchedSections(originalSentences, comparedSentences);

        // Calculate comparison metrics
        ComparisonMetrics metrics = calculateMetrics(originalSentences, comparedSentences, matchedSections);

        // Determine similarity level
        String similarityLevel = determineSimilarityLevel(overallSimilarity);

        return PlagiarismResult.builder()
                .similarityScore(Math.round(overallSimilarity * 100.0) / 100.0)
                .similarityLevel(similarityLevel)
                .originalDocument(originalInfo)
                .comparedDocument(comparedInfo)
                .matchedSections(matchedSections)
                .metrics(metrics)
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }

    private List<MatchedSection> findMatchedSections(List<String> originalSentences, List<String> comparedSentences) {
        List<MatchedSection> matches = new ArrayList<>();
        double threshold = 0.70; // 70% similarity threshold

        for (int i = 0; i < originalSentences.size(); i++) {
            String originalSentence = originalSentences.get(i);
            
            for (int j = 0; j < comparedSentences.size(); j++) {
                String comparedSentence = comparedSentences.get(j);
                
                double similarity = similarityCalculationService.calculateCosineSimilarity(
                        originalSentence, comparedSentence);

                if (similarity >= threshold) {
                    matches.add(MatchedSection.builder()
                            .originalText(originalSentence.substring(0, Math.min(200, originalSentence.length())))
                            .comparedText(comparedSentence.substring(0, Math.min(200, comparedSentence.length())))
                            .similarity(Math.round(similarity * 100 * 100.0) / 100.0)
                            .originalPosition(i)
                            .comparedPosition(j)
                            .build());
                }
            }
        }

        // Sort by similarity (highest first) and limit to top 20 matches
        return matches.stream()
                .sorted((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()))
                .limit(20)
                .collect(Collectors.toList());
    }

    private ComparisonMetrics calculateMetrics(List<String> originalSentences, 
                                               List<String> comparedSentences, 
                                               List<MatchedSection> matchedSections) {
        int totalSentences = Math.max(originalSentences.size(), comparedSentences.size());
        int matchedCount = matchedSections.size();

        double avgSimilarity = matchedSections.stream()
                .mapToDouble(MatchedSection::getSimilarity)
                .average()
                .orElse(0.0);

        double maxSimilarity = matchedSections.stream()
                .mapToDouble(MatchedSection::getSimilarity)
                .max()
                .orElse(0.0);

        double minSimilarity = matchedSections.stream()
                .mapToDouble(MatchedSection::getSimilarity)
                .min()
                .orElse(0.0);

        return ComparisonMetrics.builder()
                .totalSentencesCompared(totalSentences)
                .matchedSentences(matchedCount)
                .averageSimilarity(Math.round(avgSimilarity * 100.0) / 100.0)
                .maxSimilarity(Math.round(maxSimilarity * 100.0) / 100.0)
                .minSimilarity(Math.round(minSimilarity * 100.0) / 100.0)
                .comparisonMethod("Hybrid (Cosine + Jaccard Similarity)")
                .build();
    }

    private String determineSimilarityLevel(double score) {
        if (score >= 80) {
            return "Very High - Potential Plagiarism";
        } else if (score >= 60) {
            return "High - Significant Overlap";
        } else if (score >= 40) {
            return "Moderate - Some Similarities";
        } else if (score >= 20) {
            return "Low - Minor Similarities";
        } else {
            return "Very Low - Minimal Overlap";
        }
    }
}
