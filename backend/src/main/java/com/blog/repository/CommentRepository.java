package com.blog.repository;

import com.blog.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByArticleIdAndStatusOrderByCreatedAtAsc(Long articleId, String status);
    List<Comment> findByArticleIdOrderByCreatedAtAsc(Long articleId);
    List<Comment> findAllByOrderByCreatedAtDesc();
    long countByStatus(String status);
    long countByArticleIdAndStatus(Long articleId, String status);
}