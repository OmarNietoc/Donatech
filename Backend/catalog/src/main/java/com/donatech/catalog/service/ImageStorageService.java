package com.donatech.catalog.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class ImageStorageService {

    private final Path root = Paths.get("/app/images");

    public String store(String folder, String id, MultipartFile file) throws IOException {
        String ext = detectExtension(file.getContentType());
        Path dest = root.resolve(folder).resolve(id + "." + ext);
        Files.createDirectories(dest.getParent());
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
        return folder + "/" + id + "." + ext;
    }

    public byte[] load(String relativePath) throws IOException {
        return Files.readAllBytes(root.resolve(relativePath));
    }

    public String detectContentType(String relativePath) {
        String lower = relativePath.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }

    private String detectExtension(String contentType) {
        if (contentType == null) return "jpg";
        return switch (contentType) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }
}
