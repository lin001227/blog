package com.blog.repository;

import com.blog.entity.LanguageRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LanguageRankingRepository extends JpaRepository<LanguageRanking, Long> {

    List<LanguageRanking> findAllByOrderByRankOrderAsc();
}
