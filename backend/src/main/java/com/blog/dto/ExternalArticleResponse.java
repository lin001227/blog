package com.blog.dto;

import com.blog.entity.ExternalArticle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalArticleResponse {

    private Long id;
    private String url;
    private String title;
    private String source;
    private String coverUrl;
    private String summary;
    private String summaryType;
    private String category;
    private String tags;
    private LocalDateTime fetchedAt;
    private LocalDateTime publishedAt;
    private String status;
    private LocalDateTime createdAt;

    public static ExternalArticleResponse fromEntity(ExternalArticle article) {
        return new ExternalArticleResponse(
                article.getId(),
                article.getUrl(),
                article.getTitle(),
                article.getSource(),
                article.getCoverUrl(),
                article.getSummary(),
                article.getSummaryType(),
                article.getCategory(),
                article.getTags(),
                article.getFetchedAt(),
                article.getPublishedAt(),
                article.getStatus(),
                article.getCreatedAt()
        );
    }
}
