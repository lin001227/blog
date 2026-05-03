package com.blog.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "external_article")
public class ExternalArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1024)
    private String url;

    @Column(nullable = false)
    private String title;

    @Column(length = 100)
    private String source;

    @Column(name = "cover_url", length = 512)
    private String coverUrl;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "original_content", columnDefinition = "MEDIUMTEXT")
    private String originalContent;

    @Column(name = "summary_type", length = 20)
    private String summaryType = "ai";

    @Column(length = 50)
    private String category;

    @Column(length = 255)
    private String tags;

    @Column(name = "fetched_at")
    private LocalDateTime fetchedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(nullable = false, length = 20)
    private String status = "pending";

    @Column(name = "error_msg", length = 500)
    private String errorMsg;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
