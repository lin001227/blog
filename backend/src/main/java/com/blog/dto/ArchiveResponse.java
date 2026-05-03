package com.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArchiveResponse {
    private int year;
    private int month;
    private String monthLabel;
    private List<ArticleResponse> articles;
    private int count;
}
