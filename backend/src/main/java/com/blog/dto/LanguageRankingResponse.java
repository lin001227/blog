package com.blog.dto;

import com.blog.entity.LanguageRanking;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LanguageRankingResponse {

    private Long id;
    private Integer rankOrder;
    private String languageName;
    private Double percentage;
    private String trend;
    private String color;
    private String iconUrl;
    private String description;
    private String month;
    private LocalDateTime createdAt;

    public static LanguageRankingResponse fromEntity(LanguageRanking entity) {
        return new LanguageRankingResponse(
                entity.getId(),
                entity.getRankOrder(),
                entity.getLanguageName(),
                entity.getPercentage(),
                entity.getTrend(),
                entity.getColor(),
                entity.getIconUrl(),
                entity.getDescription(),
                entity.getMonth(),
                entity.getCreatedAt()
        );
    }
}
