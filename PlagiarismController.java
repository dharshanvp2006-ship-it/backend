package com.plagiacheck.controller;

import com.plagiacheck.dto.PlagiarismResult;
import com.plagiacheck.service.PlagiarismDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/plagiarism")
@RequiredArgsConstructor
@Slf4j
public class PlagiarismController {

    private final PlagiarismDetectionService plagiarismDetectionService;

    @PostMapping("/detect")
    public ResponseEntity<PlagiarismResult> detectPlagiarism(
            @RequestParam("originalFile") MultipartFile originalFile,
            @RequestParam("comparedFile") MultipartFile comparedFile) {
        
        log.info("Received plagiarism detection request for files: {} and {}", 
                originalFile.getOriginalFilename(), comparedFile.getOriginalFilename());

        try {
            PlagiarismResult result = plagiarismDetectionService.detectPlagiarism(originalFile, comparedFile);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            log.error("Error processing files: {}", e.getMessage());
            throw new RuntimeException("Failed to process PDF files: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("PlagiaCheck API is running");
    }
}
