package com.plagiacheck.service;

import com.plagiacheck.dto.DocumentInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Slf4j
public class PdfExtractionService {

    public String extractTextFromPdf(MultipartFile file) throws IOException {
        log.info("Extracting text from PDF: {}", file.getOriginalFilename());
        
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            log.info("Successfully extracted {} characters from {}", text.length(), file.getOriginalFilename());
            return text;
            
        } catch (IOException e) {
            log.error("Error extracting text from PDF: {}", e.getMessage());
            throw new IOException("Failed to extract text from PDF: " + e.getMessage());
        }
    }

    public DocumentInfo getDocumentInfo(MultipartFile file, String extractedText) {
        String[] sentences = extractedText.split("[.!?]+");
        String[] words = extractedText.trim().split("\\s+");
        String[] paragraphs = extractedText.split("\n\n+");

        return DocumentInfo.builder()
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .wordCount(words.length)
                .sentenceCount(sentences.length)
                .paragraphCount(paragraphs.length)
                .build();
    }

    public void validatePdfFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("File must be a PDF");
        }

        // Check file size (max 50MB)
        if (file.getSize() > 50 * 1024 * 1024) {
            throw new IllegalArgumentException("File size cannot exceed 50MB");
        }
    }
}
