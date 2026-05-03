package com.blog.controller;

import com.blog.dto.ArticleResponse;
import com.blog.repository.ArticleRepository;
import com.blog.repository.CommentRepository;
import com.blog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // Articles
        long totalArticles = articleRepository.count();
        long articlesThisMonth = articleRepository.countByCreatedAtAfter(thirtyDaysAgo);
        long articlesThisWeek = articleRepository.countByCreatedAtAfter(sevenDaysAgo);
        List<ArticleResponse> topArticles = articleRepository.findTop5ByOrderByViewCountDesc()
                .stream()
                .map(ArticleResponse::fromEntity)
                .collect(Collectors.toList());
        List<ArticleResponse> recentArticles = articleRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .limit(5)
                .map(ArticleResponse::fromEntity)
                .collect(Collectors.toList());

        long totalViews = articleRepository.findAll().stream()
                .mapToLong(a -> a.getViewCount() != null ? a.getViewCount() : 0)
                .sum();

        long pinnedCount = articleRepository.findAll().stream()
                .filter(a -> a.getPinned() != null && a.getPinned())
                .count();

        // Comments
        long totalComments = commentRepository.count();
        long pendingComments = commentRepository.countByStatus("PENDING");

        // Users
        long totalUsers = userRepository.count();

        Map<String, Object> stats = Map.of(
                "totalArticles", totalArticles,
                "articlesThisMonth", articlesThisMonth,
                "articlesThisWeek", articlesThisWeek,
                "totalViews", totalViews,
                "pinnedCount", pinnedCount,
                "totalComments", totalComments,
                "pendingComments", pendingComments,
                "totalUsers", totalUsers
        );

        return ResponseEntity.ok(Map.of(
                "stats", stats,
                "topArticles", topArticles,
                "recentArticles", recentArticles
        ));
    }
}
