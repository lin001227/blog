package com.blog.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

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

    public void testConnection() {
        try {
            mailSender.createMimeMessage();
            log.info("Mail sender connection test passed");
        } catch (Exception e) {
            log.warn("Mail sender connection test failed: {}", e.getMessage());
        }
    }
}
