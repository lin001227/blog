package com.blog.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class DeepSeekSummaryService {

    @Value("${app.deepseek.api-key}")
    private String apiKey;

    @Value("${app.deepseek.api-url}")
    private String apiUrl;

    @Value("${app.deepseek.model}")
    private String model;

    @Value("${app.deepseek.max-tokens}")
    private int maxTokens;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 使用 DeepSeek API 生成文章摘要
     */
    public String summarize(String title, String content) {
        try {
            // 截取正文前 3000 字
            String truncated = content.length() > 3000 ? content.substring(0, 3000) : content;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content",
                                    "你是一个文章摘要助手。用中文用3-5句话总结以下文章的核心内容，保持客观、简洁、信息完整。直接输出摘要，不要加\"摘要：\"等前缀。"),
                            Map.of("role", "user", "content",
                                    "文章标题: " + title + "\n\n文章正文:\n" + truncated)
                    ),
                    "max_tokens", maxTokens,
                    "temperature", 0.3
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    if (message != null && message.get("content") != null) {
                        return ((String) message.get("content")).trim();
                    }
                }
            }

            log.warn("DeepSeek API returned unexpected response: {}", response.getBody());
            return fallbackSummary(content);

        } catch (Exception e) {
            log.error("DeepSeek API call failed: {}", e.getMessage());
            return fallbackSummary(content);
        }
    }

    /**
     * 备用方案：提取前几句作为摘要（API 失败时降级）
     */
    private String fallbackSummary(String content) {
        if (content == null || content.isBlank()) return "";
        // 取前 150 字
        String clean = content.replaceAll("\\s+", " ").trim();
        return clean.substring(0, Math.min(clean.length(), 150)) + "...";
    }
}
