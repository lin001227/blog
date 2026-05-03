package com.blog.service;

import com.blog.dto.ExternalArticleRequest;
import com.blog.dto.ExternalArticleResponse;
import com.blog.dto.ExternalArticleUpdateRequest;
import com.blog.entity.ExternalArticle;
import com.blog.repository.ExternalArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalArticleService {

    private final ExternalArticleRepository repository;
    private final WebFetcherService webFetcherService;
    private final DeepSeekSummaryService deepSeekSummaryService;

    /**
     * 添加 URL 并触发抓取 + 摘要
     */
    @Transactional
    public ExternalArticleResponse addAndFetch(ExternalArticleRequest request) {
        // 检查是否已存在
        if (repository.existsByUrl(request.getUrl())) {
            throw new RuntimeException("该链接已存在");
        }

        ExternalArticle article = new ExternalArticle();
        article.setUrl(request.getUrl());
        article.setCategory(request.getCategory());
        article.setTags(request.getTags());
        article.setStatus("pending");

        try {
            // 1. 抓取网页
            WebFetcherService.FetchResult result = webFetcherService.fetch(request.getUrl());

            article.setTitle(result.title());
            article.setSource(result.source());
            article.setCoverUrl(result.coverUrl());
            article.setOriginalContent(result.content());
            article.setPublishedAt(result.publishedAt());

            // 2. 生成摘要
            String summary = deepSeekSummaryService.summarize(result.title(), result.content());
            article.setSummary(summary);

            article.setFetchedAt(LocalDateTime.now());
            article.setStatus("success");
            log.info("Successfully fetched and summarized: {}", result.title());

        } catch (Exception e) {
            article.setStatus("failed");
            article.setErrorMsg(e.getMessage());
            log.error("Failed to fetch article from {}: {}", request.getUrl(), e.getMessage());

            // 即使抓取失败也保存记录，方便后续重试
            if (article.getTitle() == null) {
                article.setTitle("抓取失败");
            }
        }

        ExternalArticle saved = repository.save(article);
        return ExternalArticleResponse.fromEntity(saved);
    }

    /**
     * 重新抓取
     */
    @Transactional
    public ExternalArticleResponse refetch(Long id) {
        ExternalArticle article = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在"));

        article.setStatus("pending");
        article.setErrorMsg(null);

        try {
            WebFetcherService.FetchResult result = webFetcherService.fetch(article.getUrl());

            article.setTitle(result.title());
            article.setSource(result.source());
            article.setCoverUrl(result.coverUrl());
            article.setOriginalContent(result.content());
            article.setPublishedAt(result.publishedAt());

            String summary = deepSeekSummaryService.summarize(result.title(), result.content());
            article.setSummary(summary);

            article.setFetchedAt(LocalDateTime.now());
            article.setStatus("success");
            log.info("Refetched: {}", result.title());

        } catch (Exception e) {
            article.setStatus("failed");
            article.setErrorMsg(e.getMessage());
        }

        return ExternalArticleResponse.fromEntity(repository.save(article));
    }

    @Transactional(readOnly = true)
    public List<ExternalArticleResponse> getAll() {
        return repository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(ExternalArticleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExternalArticleResponse> getPublicList() {
        return repository.findAllByOrderByCreatedAtDesc()
                .stream()
                .filter(a -> "success".equals(a.getStatus()))
                .map(ExternalArticleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExternalArticleResponse update(Long id, ExternalArticleUpdateRequest request) {
        ExternalArticle article = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在"));

        if (request.getSummary() != null) article.setSummary(request.getSummary());
        if (request.getCategory() != null) article.setCategory(request.getCategory());
        if (request.getTags() != null) article.setTags(request.getTags());

        return ExternalArticleResponse.fromEntity(repository.save(article));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("记录不存在");
        }
        repository.deleteById(id);
    }
}
