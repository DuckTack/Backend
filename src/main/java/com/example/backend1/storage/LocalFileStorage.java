package com.example.backend1.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Component
public class LocalFileStorage implements FileStorage {

    private final Path root;
    private final String publicBaseUrl;
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp", "pdf");

    public LocalFileStorage(
            @Value("${storage.local.root}") String rootDir,
            @Value("${storage.local.public-base-url}") String publicBaseUrl
    ) {
        this.root = Paths.get(rootDir);
        this.publicBaseUrl = publicBaseUrl;
    }

    @Override
    public StoredFile save(MultipartFile file) {
        try {
            Files.createDirectories(root);

            String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
            if (ext != null) ext = ext.toLowerCase();
            if (ext != null && !ALLOWED_EXT.contains(ext)) {
                ext = null;
            }
            String key = UUID.randomUUID() + (ext != null ? "." + ext : "");
            Path target = root.resolve(key);

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String url = getPublicUrl(key);
            return new StoredFile(key, file.getOriginalFilename(), file.getContentType(), file.getSize(), url);
        } catch (IOException e) {
            throw new RuntimeException("file save failed", e);
        }
    }

    @Override
    public StoredFile saveBytes(String filename, String contentType, byte[] bytes) {
        try {
            Files.createDirectories(root);

            String ext = StringUtils.getFilenameExtension(filename);
            if (ext != null) ext = ext.toLowerCase();
            if (ext != null && !ALLOWED_EXT.contains(ext)) {
                ext = null;
            }
            String key = UUID.randomUUID() + (ext != null ? "." + ext : "");
            Path target = root.resolve(key);

            Files.write(target, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            String url = getPublicUrl(key);
            return new StoredFile(key, filename, contentType, bytes.length, url);
        } catch (IOException e) {
            throw new RuntimeException("file saveBytes failed", e);
        }
    }

    @Override
    public byte[] load(String key) {
        try {
            if (key == null || !key.matches("^[0-9a-fA-F\\-]{36}(\\.[A-Za-z0-9]{1,10})?$")) {
                throw new IllegalArgumentException("invalid key");
            }
            Path resolved = root.resolve(key).normalize();
            if (!resolved.startsWith(root.normalize())) {
                throw new IllegalArgumentException("invalid key");
            }
            return Files.readAllBytes(resolved);
        } catch (IOException e) {
            throw new RuntimeException("file load failed", e);
        }
    }

    @Override
    public String getPublicUrl(String key) {
        return publicBaseUrl + "/api/files/" + key;
    }
}
