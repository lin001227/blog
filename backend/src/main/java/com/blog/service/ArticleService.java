package com.blog.service;

import com.blog.dto.ArticleRequest;
import com.blog.dto.ArticleResponse;
import com.blog.dto.ArticleUpdateRequest;
import com.blog.entity.Article;
import com.blog.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.blog.dto.ArchiveResponse;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;

    @Transactional(readOnly = true)
    public List<ArticleResponse> getPublicArticles() {
        return articleRepository.findAllByOrderByPinnedDescCreatedAtDesc()
                .stream()
                .map(ArticleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ArticleResponse> getAdminArticles() {
        return articleRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ArticleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ArticleResponse getArticleById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        return ArticleResponse.fromEntity(article);
    }

    @Transactional
    public ArticleResponse getArticleAndIncrementView(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        article.setViewCount(article.getViewCount() == null ? 1 : article.getViewCount() + 1);
        Article saved = articleRepository.save(article);
        return ArticleResponse.fromEntity(saved);
    }

    @Transactional
    public ArticleResponse createArticle(ArticleRequest request) {
        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setContent(request.getContent());
        article.setCategory(request.getCategory());
        article.setTags(request.getTags());
        article.setPinned(request.getPinned() != null && request.getPinned());

        Article saved = articleRepository.save(article);
        return ArticleResponse.fromEntity(saved);
    }

    @Transactional
    public ArticleResponse updateArticle(Long id, ArticleUpdateRequest request) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        if (request.getTitle() != null) {
            article.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            article.setContent(request.getContent());
        }
        if (request.getCategory() != null) {
            article.setCategory(request.getCategory());
        }
        if (request.getTags() != null) {
            article.setTags(request.getTags());
        }
        if (request.getPinned() != null) {
            article.setPinned(request.getPinned());
        }

        Article saved = articleRepository.save(article);
        return ArticleResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<ArchiveResponse> getArchive() {
        List<Article> articles = articleRepository.findAllByOrderByCreatedAtDesc();
        Map<String, List<ArticleResponse>> grouped = articles.stream()
                .map(ArticleResponse::fromEntity)
                .collect(Collectors.groupingBy(
                        a -> a.getCreatedAt().getYear() + "-" + String.format("%02d", a.getCreatedAt().getMonthValue()),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return grouped.entrySet().stream()
                .map(entry -> {
                    String[] parts = entry.getKey().split("-");
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    return ArchiveResponse.builder()
                            .year(year)
                            .month(month)
                            .monthLabel(year + "年" + month + "月")
                            .articles(entry.getValue())
                            .count(entry.getValue().size())
                            .build();
                })
                .sorted((a, b) -> {
                    if (a.getYear() != b.getYear()) return b.getYear() - a.getYear();
                    return b.getMonth() - a.getMonth();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        articleRepository.delete(article);
    }
}
