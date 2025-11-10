package com.plagiacheck.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SimilarityCalculationService {

    private final TextProcessingService textProcessingService;

    /**
     * Calculate cosine similarity between two texts using TF-IDF vectors
     */
    public double calculateCosineSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null || text1.isEmpty() || text2.isEmpty()) {
            return 0.0;
        }

        String[] tokens1 = textProcessingService.tokenize(text1);
        String[] tokens2 = textProcessingService.tokenize(text2);

        Map<String, Double> vector1 = createTfIdfVector(tokens1, tokens2);
        Map<String, Double> vector2 = createTfIdfVector(tokens2, tokens1);

        return cosineSimilarity(vector1, vector2);
    }

    /**
     * Calculate Jaccard similarity (set-based similarity)
     */
    public double calculateJaccardSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null || text1.isEmpty() || text2.isEmpty()) {
            return 0.0;
        }

        Set<String> set1 = new HashSet<>(Arrays.asList(textProcessingService.tokenize(text1)));
        Set<String> set2 = new HashSet<>(Arrays.asList(textProcessingService.tokenize(text2)));

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    /**
     * Calculate Levenshtein distance normalized as similarity score
     */
    public double calculateLevenshteinSimilarity(String text1, String text2) {
        int distance = levenshteinDistance(text1, text2);
        int maxLength = Math.max(text1.length(), text2.length());
        
        return maxLength == 0 ? 1.0 : 1.0 - ((double) distance / maxLength);
    }

    /**
     * Create TF-IDF vector for a text
     */
    private Map<String, Double> createTfIdfVector(String[] tokens, String[] corpusTokens) {
        Map<String, Double> vector = new HashMap<>();
        Map<String, Integer> termFrequency = calculateTermFrequency(tokens);
        Set<String> corpusVocabulary = new HashSet<>(Arrays.asList(corpusTokens));

        for (Map.Entry<String, Integer> entry : termFrequency.entrySet()) {
            String term = entry.getKey();
            int tf = entry.getValue();
            
            // Calculate IDF (inverse document frequency)
            double idf = corpusVocabulary.contains(term) ? 
                    Math.log(2.0) : Math.log(2.0 / 1.0);
            
            double tfIdf = tf * idf;
            vector.put(term, tfIdf);
        }

        return vector;
    }

    /**
     * Calculate term frequency
     */
    private Map<String, Integer> calculateTermFrequency(String[] tokens) {
        Map<String, Integer> frequency = new HashMap<>();
        for (String token : tokens) {
            frequency.put(token, frequency.getOrDefault(token, 0) + 1);
        }
        return frequency;
    }

    /**
     * Calculate cosine similarity between two vectors
     */
    private double cosineSimilarity(Map<String, Double> vector1, Map<String, Double> vector2) {
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(vector1.keySet());
        allKeys.addAll(vector2.keySet());

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String key : allKeys) {
            double val1 = vector1.getOrDefault(key, 0.0);
            double val2 = vector2.getOrDefault(key, 0.0);

            dotProduct += val1 * val2;
            norm1 += val1 * val1;
            norm2 += val2 * val2;
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * Calculate Levenshtein distance between two strings
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[s1.length()][s2.length()];
    }
}
