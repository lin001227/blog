package com.blog.dto;

import com.blog.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {

    private Long id;
    private Long articleId;
    private String articleTitle;
    private Long parentId;
    private String nickname;
    private String email;
    private String content;
    private String status;
    private LocalDateTime createdAt;
    private Boolean pinned;

    public static CommentResponse fromEntity(Comment comment) {
        return CommentResponse.fromEntity(comment, null);
    }

    public static CommentResponse fromEntity(Comment comment, String articleTitle) {
        return CommentResponse.builder()
                .id(comment.getId())
                .articleId(comment.getArticleId())
                .articleTitle(articleTitle)
                .parentId(comment.getParentId())
                .nickname(comment.getNickname())
                .email(comment.getEmail())
                .content(comment.getContent())
                .status(comment.getStatus())
                .createdAt(comment.getCreatedAt())
                .pinned(comment.getPinned() != null && comment.getPinned())
                .build();
    }
}