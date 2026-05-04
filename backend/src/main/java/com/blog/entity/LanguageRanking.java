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
@Table(name = "language_ranking")
public class LanguageRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rank_order", nullable = false)
    private Integer rankOrder;

    @Column(name = "language_name", nullable = false, length = 100)
    private String languageName;

    @Column(nullable = false)
    private Double percentage;

    @Column(nullable = false, length = 10)
    private String trend = "stable";

    @Column(length = 20)
    private String color;

    @Column(name = "icon_url", length = 512)
    private String iconUrl;

    @Column(length = 500)
    private String description;

    @Column(length = 7)
    private String month;

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
