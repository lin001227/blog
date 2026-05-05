package com.blog.service;

import com.blog.entity.LanguageRanking;
import com.blog.repository.LanguageRankingRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class LLMRankingFetchService {

    private final LanguageRankingRepository repository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.deepseek.api-key}")
    private String apiKey;

    @Value("${app.deepseek.api-url}")
    private String apiUrl;

    @Value("${app.deepseek.model}")
    private String model;

    /**
     * 抓取并更新大模型排行数据
     * 使用 DeepSeek API 获取最新排行
     */
    @Transactional
    public boolean fetchAndUpdate() {
        log.info("开始抓取大模型排行数据...");

        try {
            String jsonData = callDeepSeekForRankings();
            if (jsonData == null || jsonData.isBlank()) {
                log.warn("DeepSeek API 返回空数据，保持现有排行数据不变");
                return false;
            }

            List<LanguageRanking> rankings = parseRankings(jsonData);
            if (rankings.isEmpty()) {
                log.warn("解析后的排行数据为空，保持现有数据不变");
                return false;
            }

            // 更新数据库：先删除旧数据，再插入新数据
            repository.deleteAll();
            repository.flush();
            repository.saveAll(rankings);

            log.info("大模型排行数据更新成功，共 {} 条记录", rankings.size());
            return true;

        } catch (Exception e) {
            log.error("抓取大模型排行数据失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 调用 DeepSeek API 获取排行榜 JSON
     */
    private String callDeepSeekForRankings() {
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        String systemPrompt = "你是一个大模型排行榜助手。请基于你的知识给出当前最新的大语言模型 ELO 评分排行榜（10个模型）。";

        String userPrompt = """
请生成一份当前最新的（%s）大语言模型排行榜 JSON 数据，包含 Top 10 大模型。

要求：
1. 按 ELO 评分从高到低排列
2. percentage 是基于 ELO 评分的归一化百分比值（最高分模型 ≈ 28~30%，分数递减）
3. 每条数据格式如下，返回**纯 JSON 数组**，不要包含任何 markdown 标记或说明文字：

[
  {
    "language_name": "模型名称（含版本号，如 Claude 4 Sonnet）",
    "percentage": 28.5,
    "trend": "up/stable/down",
    "color": "#十六进制颜色（6位）",
    "icon_url": "https://cdn.simpleicons.org/公司英文名小写/颜色代码",
    "description": "公司名 · 简短特点描述（10字以内）"
  },
  ...
]

注意事项：
- trend 字段：相比上个月的趋势，仅填 "up"/"stable"/"down"
- color 字段：使用该模型的品牌色或代表色
- icon_url 字段：使用 https://cdn.simpleicons.org/ 格式
- percentage 精确到 1 位小数，总和大约 100%
- description 格式："公司名 · 特点"，如 "Anthropic · 全能最强编程"
- 只返回 JSON 数组，不要带任何其他文字
""".formatted(currentMonth);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "max_tokens", 2000,
                "temperature", 0.3
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    if (message != null && message.get("content") != null) {
                        String content = ((String) message.get("content")).trim();
                        // 移除可能的 markdown 代码块标记
                        content = content.replaceAll("```(?:json)?\\s*", "").trim();
                        return content;
                    }
                }
            }

            log.warn("DeepSeek API 返回异常: {}", response.getBody());
            return null;

        } catch (Exception e) {
            log.error("DeepSeek API 调用失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 解析 JSON 字符串为 LanguageRanking 实体列表
     */
    private List<LanguageRanking> parseRankings(String json) {
        List<LanguageRanking> rankings = new ArrayList<>();
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        try {
            JsonNode root = objectMapper.readTree(json);
            int rankOrder = 1;

            if (root.isArray()) {
                for (JsonNode node : root) {
                    LanguageRanking entity = new LanguageRanking();
                    entity.setRankOrder(rankOrder++);
                    entity.setLanguageName(getText(node, "language_name"));
                    entity.setPercentage(node.has("percentage") ? node.get("percentage").asDouble() : 0.0);
                    entity.setTrend(getText(node, "trend", "stable"));
                    entity.setColor(getText(node, "color"));
                    entity.setIconUrl(getText(node, "icon_url"));
                    entity.setDescription(getText(node, "description"));
                    entity.setMonth(currentMonth);
                    rankings.add(entity);
                }
            }

            log.info("解析到 {} 条排行数据", rankings.size());
            return rankings;

        } catch (JsonProcessingException e) {
            log.error("JSON 解析失败: {}", e.getMessage());
            return List.of();
        }
    }

    private String getText(JsonNode node, String field) {
        return getText(node, field, "");
    }

    private String getText(JsonNode node, String field, String defaultValue) {
        JsonNode f = node.get(field);
        return (f != null && !f.isNull()) ? f.asText() : defaultValue;
    }
}
