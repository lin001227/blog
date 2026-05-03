package com.blog.repository;

import com.blog.entity.ExternalArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExternalArticleRepository extends JpaRepository<ExternalArticle, Long> {

    List<ExternalArticle> findAllByOrderByCreatedAtDesc();

    boolean existsByUrl(String url);
}
