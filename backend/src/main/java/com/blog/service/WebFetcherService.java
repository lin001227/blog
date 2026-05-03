package com.blog.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Service
@Slf4j
public class WebFetcherService {

    private static final String USER_AGENT = "Mozilla/5.0 (compatible; BlogBot/1.0; +https://blog.example.com)";
    private static final int TIMEOUT_MS = 15000;

    public record FetchResult(String title, String source, String coverUrl,
                              String content, LocalDateTime publishedAt) {}

    /**
     * 抓取网页内容并提取关键信息
     */
    public FetchResult fetch(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_MS)
                    .followRedirects(true)
                    .get();

            String title = extractTitle(doc);
            String source = extractSource(url, doc);
            String coverUrl = extractCoverUrl(doc);
            String content = extractContent(doc);
            LocalDateTime publishedAt = extractPublishedAt(doc);

            return new FetchResult(title, source, coverUrl, content, publishedAt);

        } catch (Exception e) {
            log.error("Failed to fetch URL: {}", url, e);
            throw new RuntimeException("抓取失败: " + e.getMessage());
        }
    }

    private String extractTitle(Document doc) {
        // OG title first
        Element ogTitle = doc.selectFirst("meta[property=og:title]");
        if (ogTitle != null) {
            String t = ogTitle.attr("content");
            if (!t.isBlank()) return t;
        }
        // Then <title>
        String title = doc.title();
        if (!title.isBlank()) return title;
        // Then h1
        Element h1 = doc.selectFirst("h1");
        if (h1 != null) return h1.text();
        return "未知标题";
    }

    private String extractSource(String url, Document doc) {
        // Try og:site_name
        Element ogSite = doc.selectFirst("meta[property=og:site_name]");
        if (ogSite != null) {
            String s = ogSite.attr("content");
            if (!s.isBlank()) return s;
        }
        // Fallback: domain name
        try {
            String domain = new java.net.URL(url).getHost();
            return domain.replace("www.", "");
        } catch (Exception e) {
            return url;
        }
    }

    private String extractCoverUrl(Document doc) {
        // OG image
        Element ogImage = doc.selectFirst("meta[property=og:image]");
        if (ogImage != null) {
            String img = ogImage.attr("content");
            if (!img.isBlank()) return img;
        }
        // Twitter image
        Element twImage = doc.selectFirst("meta[name=twitter:image]");
        if (twImage != null) {
            String img = twImage.attr("content");
            if (!img.isBlank()) return img;
        }
        // First large image in article
        Element img = doc.selectFirst("article img[src], main img[src], .content img[src]");
        if (img != null) {
            String src = img.attr("src");
            if (src.startsWith("//")) src = "https:" + src;
            if (!src.startsWith("http")) {
                // Try to resolve relative URL
                try {
                    src = doc.selectFirst("base") != null
                            ? doc.selectFirst("base").attr("href") + src
                            : src;
                } catch (Exception ignored) {}
            }
            return src;
        }
        return null;
    }

    private String extractContent(Document doc) {
        // Remove unwanted elements
        doc.select("script, style, nav, footer, header, aside, " +
                   ".sidebar, .menu, .comments, .ad, .advertisement, " +
                   "noscript, iframe, form, button, svg").remove();

        // Try article tag first
        Element article = doc.selectFirst("article");
        if (article != null) {
            return article.text();
        }
        // Then main
        Element main = doc.selectFirst("main");
        if (main != null) {
            return main.text();
        }
        // Then .content, .post, .article
        Elements content = doc.select(".content, .post, .article, .entry-content");
        if (!content.isEmpty()) {
            return content.first().text();
        }
        // Fallback: body
        return doc.body().text();
    }

    private LocalDateTime extractPublishedAt(Document doc) {
        // Try article:published_time
        Element ogTime = doc.selectFirst("meta[property=article:published_time]");
        if (ogTime != null) {
            return parseDateTime(ogTime.attr("content"));
        }
        // Try meta[name=date]
        Element metaDate = doc.selectFirst("meta[name=date]");
        if (metaDate != null) {
            return parseDateTime(metaDate.attr("content"));
        }
        // Try time tag
        Element timeTag = doc.selectFirst("time[datetime]");
        if (timeTag != null) {
            return parseDateTime(timeTag.attr("datetime"));
        }
        return null;
    }

    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        // Try ISO format
        try {
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException ignored) {}

        // Try date-only format
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE);
            return date.atStartOfDay();
        } catch (DateTimeParseException ignored) {}

        return null;
    }
}
