package com.blog.service;

import com.blog.dto.ArticleResponse;
import com.blog.repository.SubscriberRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final SubscriberRepository subscriberRepository;

    @Value("${app.mail.from}")
    private String fromAddress;

    @Value("${app.blog.url:https://localhost}")
    private String blogUrl;

    @Value("${app.blog.name:风屿 · 随笔}")
    private String blogName;

    @Async
    public void sendWelcomeEmail(String toEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromAddress, blogName);
            helper.setTo(toEmail);
            helper.setSubject("🎉 感谢订阅「" + blogName + "」");

            helper.setText(buildWelcomeHtml(toEmail), true);

            mailSender.send(message);
            log.info("Welcome email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendNewArticleNotification(ArticleResponse article) {
        List<String> emails = subscriberRepository.findAllActiveEmails();
        if (emails.isEmpty()) {
            log.info("No active subscribers to notify for article: {}", article.getTitle());
            return;
        }

        String subject = "📝 新文章发布：「" + article.getTitle() + "」";
        String html = buildNewArticleHtml(article);

        log.info("Sending new article notification to {} subscribers for: {}", emails.size(), article.getTitle());

        int successCount = 0;
        for (String email : emails) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                helper.setFrom(fromAddress, blogName);
                helper.setTo(email);
                helper.setSubject(subject);
                helper.setText(html, true);
                mailSender.send(message);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to send article notification to {}: {}", email, e.getMessage());
            }
        }

        log.info("Article notification sent: {}/{} successful for '{}'", successCount, emails.size(), article.getTitle());
    }

    private String buildWelcomeHtml(String email) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8" /></head>
            <body style="margin:0;padding:0;background-color:#fafaf9;font-family:'Noto Serif SC','Inter','Helvetica Neue',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#fafaf9;padding:40px 16px;">
                <tr>
                  <td align="center">
                    <table width="560" cellpadding="0" cellspacing="0" style="max-width:560px;width:100%%;background-color:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 1px 4px rgba(0,0,0,0.04);border:1px solid #e7e5e4;">
                      <!-- Header -->
                      <tr>
                        <td style="padding:40px 40px 0;text-align:center;">
                          <div style="font-size:36px;line-height:1;margin-bottom:12px;">🌊</div>
                          <h1 style="font-family:'Noto Serif SC',serif;font-size:22px;font-weight:700;color:#1c1917;margin:0 0 4px;letter-spacing:1px;">%s</h1>
                          <p style="font-size:14px;color:#a8a29e;margin:0;">记录思考，分享见解</p>
                        </td>
                      </tr>
                      <!-- Divider -->
                      <tr>
                        <td style="padding:24px 40px 0;">
                          <div style="height:1px;background:linear-gradient(to right,transparent,#e7e5e4,transparent);"></div>
                        </td>
                      </tr>
                      <!-- Content -->
                      <tr>
                        <td style="padding:28px 40px 0;">
                          <p style="font-size:15px;color:#292524;line-height:1.8;margin:0 0 16px;">
                            你好 👋
                          </p>
                          <p style="font-size:15px;color:#292524;line-height:1.8;margin:0 0 16px;">
                            感谢你订阅 <strong style="color:#1c1917;">「%s」</strong>！今后有新文章发布时，你会第一时间收到推送通知。
                          </p>
                          <p style="font-size:15px;color:#78716c;line-height:1.8;margin:0 0 4px;">
                            已订阅邮箱：
                          </p>
                          <div style="background:#f5f5f4;border-radius:8px;padding:12px 16px;margin-bottom:20px;">
                            <code style="font-size:14px;color:#1c1917;font-weight:500;">%s</code>
                          </div>
                        </td>
                      </tr>
                      <!-- CTA Buttons -->
                      <tr>
                        <td style="padding:0 40px 32px;">
                          <table width="100%%" cellpadding="0" cellspacing="0">
                            <tr>
                              <td align="center" style="padding-bottom:12px;">
                                <a href="%s" style="display:inline-block;padding:12px 28px;background-color:#2563eb;color:#ffffff;text-decoration:none;border-radius:8px;font-size:14px;font-weight:600;letter-spacing:0.5px;">浏览最新文章 →</a>
                              </td>
                            </tr>
                            <tr>
                              <td align="center" style="font-size:12px;">
                                <a href="%s/archive" style="color:#78716c;text-decoration:none;margin:0 8px;">归档</a>
                                <span style="color:#e7e5e4;">·</span>
                                <a href="%s/rankings" style="color:#78716c;text-decoration:none;margin:0 8px;">排行榜</a>
                                <span style="color:#e7e5e4;">·</span>
                                <a href="%s/readings" style="color:#78716c;text-decoration:none;margin:0 8px;">精选阅读</a>
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>
                      <!-- Footer -->
                      <tr>
                        <td style="background:#fafaf9;padding:24px 40px;text-align:center;border-top:1px solid #e7e5e4;">
                          <p style="font-size:12px;color:#a8a29e;line-height:1.6;margin:0;">
                            这封邮件由 <strong style="color:#78716c;">%s</strong> 自动发送。<br>
                            如果你未进行此操作，请忽略此邮件。
                          </p>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(blogName, blogName, email, blogUrl, blogUrl, blogUrl, blogUrl, blogName);
    }

    private String buildNewArticleHtml(ArticleResponse article) {
        // Extract a clean plain-text excerpt from markdown content
        String excerpt = extractExcerpt(article.getContent(), 200);

        String articleUrl = blogUrl + "/article/" + article.getId();

        // Build tags display
        String tagsHtml = "";
        if (article.getTags() != null && !article.getTags().isBlank()) {
            String[] tags = article.getTags().split(",");
            StringBuilder sb = new StringBuilder();
            for (String tag : tags) {
                String t = tag.trim();
                if (!t.isEmpty()) {
                    sb.append("<span style=\"display:inline-block;padding:3px 10px;background:#eff6ff;color:#2563eb;border-radius:12px;font-size:12px;margin:0 4px 4px 0;\">")
                      .append(t)
                      .append("</span>");
                }
            }
            tagsHtml = sb.toString();
        }

        // Category display
        String categoryBadge = "";
        if (article.getCategory() != null && !article.getCategory().isBlank()) {
            categoryBadge = "<span style=\"font-size:13px;color:#78716c;\">📂 " + article.getCategory() + "</span>";
        }

        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8" /></head>
            <body style="margin:0;padding:0;background-color:#fafaf9;font-family:'Noto Serif SC','Inter','Helvetica Neue',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#fafaf9;padding:40px 16px;">
                <tr>
                  <td align="center">
                    <table width="560" cellpadding="0" cellspacing="0" style="max-width:560px;width:100%%;background-color:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 1px 4px rgba(0,0,0,0.04);border:1px solid #e7e5e4;">
                      <!-- Header -->
                      <tr>
                        <td style="background:linear-gradient(135deg,#2563eb,#1d4ed8);padding:32px 40px;text-align:center;">
                          <div style="font-size:32px;line-height:1;margin-bottom:8px;">📝</div>
                          <h1 style="font-family:'Noto Serif SC',serif;font-size:18px;font-weight:600;color:#ffffff;margin:0;letter-spacing:0.5px;">%s</h1>
                          <p style="font-size:13px;color:#93c5fd;margin:8px 0 0;letter-spacing:0.3px;">新文章推送 · 第一时间送达</p>
                        </td>
                      </tr>
                      <!-- Article Content -->
                      <tr>
                        <td style="padding:32px 40px 24px;">
                          <!-- Title -->
                          <h2 style="font-family:'Noto Serif SC',serif;font-size:20px;font-weight:700;color:#1c1917;margin:0 0 12px;line-height:1.4;">
                            <a href="%s" style="color:#1c1917;text-decoration:none;">%s</a>
                          </h2>
                          <!-- Meta -->
                          <table cellpadding="0" cellspacing="0" style="margin-bottom:16px;">
                            <tr>
                              <td style="padding-right:16px;">%s</td>
                              <td><span style="font-size:13px;color:#78716c;">📅 %s</span></td>
                            </tr>
                          </table>
                          <!-- Tags -->
                          %s
                          <!-- Divider -->
                          <div style="height:1px;background:linear-gradient(to right,transparent,#e7e5e4,transparent);margin:20px 0 24px;"></div>
                          <!-- Excerpt -->
                          <p style="font-size:15px;color:#57534e;line-height:1.8;margin:0 0 24px;">%s</p>
                          <!-- CTA -->
                          <table width="100%%" cellpadding="0" cellspacing="0">
                            <tr>
                              <td align="center">
                                <a href="%s" style="display:inline-block;padding:12px 32px;background-color:#2563eb;color:#ffffff;text-decoration:none;border-radius:8px;font-size:14px;font-weight:600;letter-spacing:0.5px;">阅读全文 →</a>
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>
                      <!-- Footer -->
                      <tr>
                        <td style="background:#fafaf9;padding:24px 40px;text-align:center;border-top:1px solid #e7e5e4;">
                          <p style="font-size:12px;color:#a8a29e;line-height:1.6;margin:0 0 8px;">
                            这封邮件由 <strong style="color:#78716c;">%s</strong> 自动发送
                          </p>
                          <p style="font-size:11px;color:#d6d3d1;margin:0;">
                            如果你不想再收到此类邮件，可以<a href="%s/unsubscribe" style="color:#a8a29e;text-decoration:underline;">退订</a>
                          </p>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(
                blogName,
                articleUrl, article.getTitle(),
                categoryBadge,
                article.getCreatedAt().toLocalDate().toString(),
                tagsHtml,
                excerpt,
                articleUrl,
                blogName,
                blogUrl
            );
    }

    /**
     * Extract a plain-text excerpt from markdown content.
     * Strips markdown syntax (headers, bold, links, code blocks, etc.)
     * and returns the first {@code maxLen} characters.
     */
    private String extractExcerpt(String markdown, int maxLen) {
        if (markdown == null || markdown.isBlank()) {
            return "（暂无摘要）";
        }
        String text = markdown
                .replace("\\n", "\n")                          // fix literal \n -> real newlines
                .replaceAll("```[\\s\\S]*?```", "")            // code blocks
                .replaceAll("!\\[[^\\]]*\\]\\([^)]*\\)", "")  // images
                .replaceAll("\\[([^\\]]*)\\]\\([^)]*\\)", "$1") // links
                .replaceAll("(?m)^#{1,6}\\s+", "")            // headers
                .replaceAll("\\*\\*(.+?)\\*\\*", "$1")        // bold
                .replaceAll("\\*(.+?)\\*", "$1")              // italic
                .replaceAll("`([^`]+)`", "$1")                // inline code
                .replaceAll("(?m)^[\\s>|]*[-*+]\\s+", "")     // list items
                .replaceAll("(?m)^>\\s+", "")                 // blockquotes
                .replaceAll("\\n+", " ")                      // newlines -> spaces
                .replaceAll("\\s{2,}", " ")                   // collapse whitespace
                .trim();

        if (text.length() <= maxLen) {
            return text;
        }
        // Try to break at a sentence boundary
        int breakAt = text.lastIndexOf("。", maxLen);
        if (breakAt > maxLen / 2) {
            return text.substring(0, breakAt + 1) + "……";
        }
        // Fallback: break at word boundary
        int wordBreak = text.lastIndexOf(" ", maxLen);
        if (wordBreak > maxLen / 2) {
            return text.substring(0, wordBreak) + "……";
        }
        return text.substring(0, maxLen) + "……";
    }

    public void testConnection() {
        try {
            mailSender.createMimeMessage();
            log.info("Mail sender connection test passed");
        } catch (Exception e) {
            log.warn("Mail sender connection test failed: {}", e.getMessage());
        }
    }
}
