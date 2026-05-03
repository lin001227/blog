package com.blog.dto;

import com.blog.entity.Article;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleResponse {

    private Long id;
    private String title;
    private String content;
    private String category;
    private String tags;
    private Boolean pinned;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer viewCount;
    private Integer commentCount;

    public static ArticleResponse fromEntity(Article article) {
        return ArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .category(article.getCategory())
                .tags(article.getTags())
                .pinned(article.getPinned())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .viewCount(article.getViewCount() == null ? 0 : article.getViewCount())
                .commentCount(0)
                .build();
    }

    public static ArticleResponse fromEntity(Article article, Integer commentCount) {
        return ArticleResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .category(article.getCategory())
                .tags(article.getTags())
                .pinned(article.getPinned())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .viewCount(article.getViewCount() == null ? 0 : article.getViewCount())
                .commentCount(commentCount == null ? 0 : commentCount)
                .build();
    }
}
