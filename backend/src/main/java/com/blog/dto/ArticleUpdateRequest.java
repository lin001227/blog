package com.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleUpdateRequest {

    private String title;

    private String content;

    private String category;

    private String tags;

    private Boolean pinned;
}
