package com.blog.repository;

import com.blog.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    List<Article> findAllByOrderByPinnedDescCreatedAtDesc();

    List<Article> findAllByOrderByCreatedAtDesc();

    List<Article> findTop5ByOrderByViewCountDesc();

    long countByCreatedAtAfter(LocalDateTime dateTime);
}
