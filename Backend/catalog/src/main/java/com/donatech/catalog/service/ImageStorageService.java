package com.donatech.catalog.service;

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

    private final Path root = Paths.get("/app/images");

    private static final String CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final Random RANDOM = new Random();

    /** Para productos y kits — usa el ID de entidad como nombre de archivo (compatibilidad existente). */
    public String store(String folder, String entityId, MultipartFile file) throws IOException {
        String ext = detectExtension(file.getContentType());
        Path dest = root.resolve(folder).resolve(entityId + "." + ext);
        Files.createDirectories(dest.getParent());
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
        return folder + "/" + entityId + "." + ext;
    }

    /** Para campañas, avatars y comprobantes — nombre único basado en email + fecha + random. */
    public String storeUnique(String folder, String uploaderEmail, MultipartFile file) throws IOException {
        String sanitized = uploaderEmail.replace("@", "_at_").replace(".", "_");
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String random = randomAlphanumeric(6);
        String ext = detectExtension(file.getContentType());
        String filename = sanitized + "_" + date + "_" + random + "." + ext;
        Path dest = root.resolve(folder).resolve(filename);
        Files.createDirectories(dest.getParent());
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
        return folder + "/" + filename;
    }

    public byte[] load(String relativePath) throws IOException {
        return Files.readAllBytes(root.resolve(relativePath));
    }

    public void delete(String relativePath) {
        if (relativePath == null) return;
        try {
            Files.deleteIfExists(root.resolve(relativePath));
        } catch (IOException ignored) {}
    }

    public String detectContentType(String relativePath) {
        if (relativePath == null) return "application/octet-stream";
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

    private String randomAlphanumeric(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
