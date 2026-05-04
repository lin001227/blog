package com.blog.controller;

import com.blog.dto.LanguageRankingRequest;
import com.blog.dto.LanguageRankingResponse;
import com.blog.service.LanguageRankingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class LanguageRankingController {

    private final LanguageRankingService service;

    // ========== Public ==========

    @GetMapping("/api/language-rankings")
    public ResponseEntity<List<LanguageRankingResponse>> getPublicList() {
        return ResponseEntity.ok(service.getPublicList());
    }

    // ========== Admin ==========

    @GetMapping("/api/admin/language-rankings")
    public ResponseEntity<List<LanguageRankingResponse>> getAdminList() {
        return ResponseEntity.ok(service.getAll());
    }

    @PostMapping("/api/admin/language-rankings")
    public ResponseEntity<LanguageRankingResponse> create(@Valid @RequestBody LanguageRankingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/api/admin/language-rankings/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                     @Valid @RequestBody LanguageRankingRequest request) {
        try {
            return ResponseEntity.ok(service.update(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/api/admin/language-rankings/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/api/admin/language-rankings/reorder")
    public ResponseEntity<List<LanguageRankingResponse>> reorder(
            @RequestBody List<Map<String, Object>> orderList) {
        return ResponseEntity.ok(service.reorder(orderList));
    }
}
