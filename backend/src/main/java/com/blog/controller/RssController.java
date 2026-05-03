package com.blog.controller;

import com.blog.dto.ArticleResponse;
import com.blog.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class RssController {

    private final ArticleService articleService;

    @GetMapping(value = "/api/rss", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> getRssFeed() {
        List<ArticleResponse> articles = articleService.getPublicArticles();

        DateTimeFormatter df = DateTimeFormatter.RFC_1123_DATE_TIME;

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n");
        xml.append("<channel>\n");
        xml.append("  <title>风屿 · 随笔</title>\n");
        xml.append("  <link>https://localhost/</link>\n");
        xml.append("  <description>记录思考，分享见解，在文字中找到宁静。</description>\n");
        xml.append("  <language>zh-CN</language>\n");
        xml.append("  <atom:link href=\"https://localhost/api/rss\" rel=\"self\" type=\"application/rss+xml\"/>\n");
        xml.append("  <lastBuildDate>").append(java.time.ZonedDateTime.now().format(df)).append("</lastBuildDate>\n");

        for (ArticleResponse article : articles) {
            xml.append("  <item>\n");
            xml.append("    <title><![CDATA[").append(escapeXml(article.getTitle())).append("]]></title>\n");
            xml.append("    <link>https://localhost/article/").append(article.getId()).append("</link>\n");
            xml.append("    <guid isPermaLink=\"true\">https://localhost/article/").append(article.getId()).append("</guid>\n");
            if (article.getCreatedAt() != null) {
                xml.append("    <pubDate>").append(article.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).format(df)).append("</pubDate>\n");
            }
            if (article.getCategory() != null) {
                xml.append("    <category><![CDATA[").append(escapeXml(article.getCategory())).append("]]></category>\n");
            }
            String description = article.getContent();
            if (description != null && description.length() > 300) {
                description = description.substring(0, 300) + "...";
            }
            xml.append("    <description><![CDATA[").append(escapeXml(description != null ? description : "")).append("]]></description>\n");
            xml.append("  </item>\n");
        }

        xml.append("</channel>\n");
        xml.append("</rss>\n");
        return ResponseEntity.ok(xml.toString());
    }

    private String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
