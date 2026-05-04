package com.blog.service;

import com.blog.dto.LanguageRankingRequest;
import com.blog.dto.LanguageRankingResponse;
import com.blog.entity.LanguageRanking;
import com.blog.repository.LanguageRankingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LanguageRankingService {

    private final LanguageRankingRepository repository;

    @Transactional(readOnly = true)
    public List<LanguageRankingResponse> getPublicList() {
        return repository.findAllByOrderByRankOrderAsc()
                .stream()
                .map(LanguageRankingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LanguageRankingResponse> getAll() {
        return repository.findAllByOrderByRankOrderAsc()
                .stream()
                .map(LanguageRankingResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public LanguageRankingResponse create(LanguageRankingRequest request) {
        LanguageRanking entity = new LanguageRanking();
        applyRequest(entity, request);
        return LanguageRankingResponse.fromEntity(repository.save(entity));
    }

    @Transactional
    public LanguageRankingResponse update(Long id, LanguageRankingRequest request) {
        LanguageRanking entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在"));
        applyRequest(entity, request);
        return LanguageRankingResponse.fromEntity(repository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("记录不存在");
        }
        repository.deleteById(id);
    }

    @Transactional
    public List<LanguageRankingResponse> reorder(List<Map<String, Object>> orderList) {
        for (Map<String, Object> item : orderList) {
            Long id = Long.valueOf(item.get("id").toString());
            Integer newOrder = Integer.valueOf(item.get("rankOrder").toString());
            repository.findById(id).ifPresent(e -> {
                e.setRankOrder(newOrder);
                repository.save(e);
            });
        }
        return getAll();
    }

    private void applyRequest(LanguageRanking entity, LanguageRankingRequest request) {
        entity.setRankOrder(request.getRankOrder());
        entity.setLanguageName(request.getLanguageName());
        entity.setPercentage(request.getPercentage());
        entity.setTrend(request.getTrend() != null ? request.getTrend() : "stable");
        entity.setColor(request.getColor());
        entity.setIconUrl(request.getIconUrl());
        entity.setDescription(request.getDescription());
        entity.setMonth(request.getMonth());
    }
}
