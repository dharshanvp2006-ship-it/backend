package com.plagiacheck.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TextProcessingService {

    public String cleanText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        // Remove extra whitespace and normalize line breaks
        text = text.replaceAll("\\s+", " ");
        text = text.replaceAll("[\r\n]+", " ");
        
        // Remove special characters but keep basic punctuation
        text = text.replaceAll("[^a-zA-Z0-9.,!?;:\\s-]", "");
        
        return text.trim();
    }

    public List<String> splitIntoSentences(String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Split by sentence-ending punctuation
        String[] sentences = text.split("[.!?]+");
        
        return Arrays.stream(sentences)
                .map(String::trim)
                .filter(s -> !s.isEmpty() && s.length() > 10) // Filter out very short fragments
                .collect(Collectors.toList());
    }

    public List<String> extractNGrams(String text, int n) {
        String[] words = text.toLowerCase().split("\\s+");
        List<String> nGrams = new ArrayList<>();
        
        for (int i = 0; i <= words.length - n; i++) {
            StringBuilder nGram = new StringBuilder();
            for (int j = 0; j < n; j++) {
                if (j > 0) nGram.append(" ");
                nGram.append(words[i + j]);
            }
            nGrams.add(nGram.toString());
        }
        
        return nGrams;
    }

    public String[] tokenize(String text) {
        return text.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .trim()
                .split("\\s+");
    }
}
