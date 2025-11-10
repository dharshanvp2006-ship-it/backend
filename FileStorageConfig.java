package com.plagiacheck.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileStorageConfig {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.temp.dir}")
    private String tempDir;

    @PostConstruct
    public void init() throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        Path tempPath = Paths.get(tempDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        if (!Files.exists(tempPath)) {
            Files.createDirectories(tempPath);
        }
    }

    public String getUploadDir() {
        return uploadDir;
    }

    public String getTempDir() {
        return tempDir;
    }
}
