package com.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {

    @NotNull(message = "Article ID is required")
    private Long articleId;

    private Long parentId;

    @NotBlank(message = "Nickname is required")
    @Size(max = 50, message = "Nickname too long")
    private String nickname;

    @Size(max = 255)
    private String email;

    @NotBlank(message = "Content is required")
    @Size(max = 2000, message = "Content too long (max 2000 chars)")
    private String content;
}