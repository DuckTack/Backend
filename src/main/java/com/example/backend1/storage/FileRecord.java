package com.example.backend1.storage;

import com.example.backend1.user.domain.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "file_record", indexes = {
        @Index(name = "idx_file_record_user_created", columnList = "user_id, createdAt"),
        @Index(name = "idx_file_record_category", columnList = "category")
})
public class FileRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FileCategory category = FileCategory.UPLOAD;

    @Column(nullable = false, unique = true, length = 80)
    private String storageKey;

    @Column(nullable = false, length = 255)
    private String originalName;

    @Column(nullable = false, length = 120)
    private String contentType;

    @Column(nullable = false)
    private long sizeBytes;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected FileRecord() {}

    public FileRecord(User user, FileCategory category, StoredFile storedFile) {
        this.user = user;
        this.category = category;
        this.storageKey = storedFile.key();
        this.originalName = (storedFile.originalName() != null) ? storedFile.originalName() : "file";
        this.contentType = (storedFile.contentType() != null) ? storedFile.contentType() : "application/octet-stream";
        this.sizeBytes = storedFile.sizeBytes();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public FileCategory getCategory() { return category; }
    public String getStorageKey() { return storageKey; }
    public String getOriginalName() { return originalName; }
    public String getContentType() { return contentType; }
    public long getSizeBytes() { return sizeBytes; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}

