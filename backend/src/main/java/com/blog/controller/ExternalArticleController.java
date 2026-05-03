package com.blog.controller;

import com.blog.dto.ExternalArticleRequest;
import com.blog.dto.ExternalArticleResponse;
import com.blog.dto.ExternalArticleUpdateRequest;
import com.blog.service.ExternalArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ExternalArticleController {

    private final ExternalArticleService service;

    // ========== Public ==========

    @GetMapping("/api/external-articles")
    public ResponseEntity<List<ExternalArticleResponse>> getPublicList() {
        return ResponseEntity.ok(service.getPublicList());
    }

    // ========== Admin ==========

    @GetMapping("/api/admin/external-articles")
    public ResponseEntity<List<ExternalArticleResponse>> getAdminList() {
        return ResponseEntity.ok(service.getAll());
    }

    @PostMapping("/api/admin/external-articles")
    public ResponseEntity<?> create(@Valid @RequestBody ExternalArticleRequest request) {
        try {
            ExternalArticleResponse response = service.addAndFetch(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/api/admin/external-articles/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody ExternalArticleUpdateRequest request) {
        try {
            return ResponseEntity.ok(service.update(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/admin/external-articles/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/admin/external-articles/{id}/refetch")
    public ResponseEntity<?> refetch(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.refetch(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
