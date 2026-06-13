package com.donatech.users.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
public class ImageStorageService {

    @Value("${app.images.root:/app/images}") private String rootPath;

    private static final String CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random RANDOM = new Random();

    public String store(String folder, String uploaderEmail, MultipartFile file) throws IOException {
        String sanitized = uploaderEmail.replace("@", "_at_").replace(".", "_");
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String random = randomAlphanumeric(6);
        String ext = detectExtension(file.getContentType(), file.getOriginalFilename());
        String filename = sanitized + "_" + date + "_" + random + "." + ext;
        Path target = Paths.get(rootPath, folder, filename);
        Files.createDirectories(target.getParent());
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return folder + "/" + filename;
    }

    public byte[] load(String relativePath) throws IOException {
        return Files.readAllBytes(Paths.get(rootPath, relativePath));
    }

    public void delete(String relativePath) {
        if (relativePath == null) return;
        try {
            Files.deleteIfExists(Paths.get(rootPath, relativePath));
        } catch (IOException ignored) {}
    }

    public String detectContentType(String relativePath) {
        if (relativePath == null) return "application/octet-stream";
        String lower = relativePath.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }

    private String detectExtension(String contentType, String originalFilename) {
        if (contentType != null) {
            return switch (contentType) {
                case "image/png" -> "png";
                case "image/webp" -> "webp";
                default -> "jpg";
            };
        }
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        }
        return "jpg";
    }

    private String randomAlphanumeric(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
