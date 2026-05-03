package com.blog.controller;

import com.blog.dto.ArchiveResponse;
import com.blog.dto.ArticleRequest;
import com.blog.dto.ArticleResponse;
import com.blog.dto.ArticleUpdateRequest;
import com.blog.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    // ========== Public endpoints ==========

    @GetMapping("/api/articles")
    public ResponseEntity<List<ArticleResponse>> getPublicArticles() {
        return ResponseEntity.ok(articleService.getPublicArticles());
    }

    @GetMapping("/api/articles/archive")
    public ResponseEntity<List<ArchiveResponse>> getArchive() {
        return ResponseEntity.ok(articleService.getArchive());
    }

    @GetMapping("/api/articles/{id}")
    public ResponseEntity<?> getArticle(@PathVariable Long id) {
        try {
            ArticleResponse response = articleService.getArticleAndIncrementView(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ========== Admin endpoints ==========

    @GetMapping("/api/admin/articles")
    public ResponseEntity<List<ArticleResponse>> getAdminArticles() {
        return ResponseEntity.ok(articleService.getAdminArticles());
    }

    @PostMapping("/api/admin/articles")
    public ResponseEntity<ArticleResponse> createArticle(@Valid @RequestBody ArticleRequest request) {
        ArticleResponse response = articleService.createArticle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/api/admin/articles/{id}")
    public ResponseEntity<?> updateArticle(@PathVariable Long id,
                                           @RequestBody ArticleUpdateRequest request) {
        try {
            ArticleResponse response = articleService.updateArticle(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/admin/articles/{id}")
    public ResponseEntity<?> deleteArticle(@PathVariable Long id) {
        try {
            articleService.deleteArticle(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }
}
