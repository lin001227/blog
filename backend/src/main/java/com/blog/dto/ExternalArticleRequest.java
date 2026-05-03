package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalArticleRequest {

    @NotBlank(message = "URL is required")
    private String url;

    private String category;

    private String tags;
}
