package com.blog.repository;

import com.blog.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.repository.query.Param;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    List<Article> findAllByOrderByPinnedDescCreatedAtDesc();

    List<Article> findAllByOrderByCreatedAtDesc();

    List<Article> findTop5ByOrderByViewCountDesc();

    long countByCreatedAtAfter(LocalDateTime dateTime);

    @Query("SELECT a FROM Article a WHERE LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(a.content) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY a.createdAt DESC")
    List<Article> searchByKeyword(@Param("query") String query);

    @Query("SELECT a FROM Article a WHERE " +
           "(:keyword IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY a.createdAt DESC")
    Page<Article> searchAdminArticles(@Param("keyword") String keyword, Pageable pageable);
}
