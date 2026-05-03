package com.blog.service;

import com.blog.dto.CommentRequest;
import com.blog.dto.CommentResponse;
import com.blog.entity.Article;
import com.blog.entity.Comment;
import com.blog.repository.ArticleRepository;
import com.blog.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;

    @Transactional(readOnly = true)
    public List<CommentResponse> getAllComments() {
        List<Comment> comments = commentRepository.findAllByOrderByPinnedDescCreatedAtDesc();
        // Batch load article titles
        List<Long> articleIds = comments.stream()
                .map(Comment::getArticleId)
                .distinct()
                .collect(Collectors.toList());
        java.util.Map<Long, String> articleTitles = articleRepository.findAllById(articleIds).stream()
                .collect(Collectors.toMap(Article::getId, Article::getTitle));
        return comments.stream()
                .map(c -> CommentResponse.fromEntity(c, articleTitles.get(c.getArticleId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getPublicComments(Long articleId) {
        String articleTitle = articleRepository.findById(articleId)
                .map(Article::getTitle)
                .orElse(null);
        return commentRepository.findByArticleIdAndStatusOrderByPinnedDescCreatedAtAsc(articleId, "APPROVED")
                .stream()
                .map(c -> CommentResponse.fromEntity(c, articleTitle))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getPendingCount() {
        return commentRepository.countByStatus("PENDING");
    }

    @Transactional
    public CommentResponse createComment(CommentRequest request) {
        Article article = articleRepository.findById(request.getArticleId())
                .orElseThrow(() -> new RuntimeException("Article not found"));

        Comment comment = Comment.builder()
                .articleId(request.getArticleId())
                .parentId(request.getParentId())
                .nickname(request.getNickname())
                .email(request.getEmail())
                .content(request.getContent())
                .build();

        Comment saved = commentRepository.save(comment);
        return CommentResponse.fromEntity(saved);
    }

    @Transactional
    public CommentResponse approveComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        comment.setStatus("APPROVED");
        return CommentResponse.fromEntity(commentRepository.save(comment));
    }

    @Transactional
    public CommentResponse rejectComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        comment.setStatus("REJECTED");
        return CommentResponse.fromEntity(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        commentRepository.delete(comment);
    }

    @Transactional
    public CommentResponse togglePin(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        comment.setPinned(!Boolean.TRUE.equals(comment.getPinned()));
        return CommentResponse.fromEntity(commentRepository.save(comment));
    }
}