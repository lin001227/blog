package com.blog.config;

import com.blog.entity.Article;
import com.blog.entity.User;
import com.blog.repository.ArticleRepository;
import com.blog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Initialize admin user if not exists
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .displayName("管理员")
                    .email("admin@blog.com")
                    .role("ADMIN")
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            log.info("Default admin user created: admin / admin123");

            // Create sample articles only if none exist
            if (articleRepository.count() == 0) {
                createSampleArticles();
            }
        }
    }

    private void createSampleArticles() {
        articleRepository.save(createArticle(
                "从零搭建个人博客：我的技术栈选择",
                "## 为什么选择自己搭建博客？\n\n在这个信息爆炸的时代，拥有一个属于自己的写作空间是一件很有意义的事情。\n\n## 技术栈\n\n- **前端框架**: Vue 3\n- **UI 库**: Element Plus\n- **后端**: Spring Boot 3\n- **数据库**: MariaDB\n- **部署**: Docker Compose\n\n> 技术选型没有绝对的对错，只有适合与不适合。",
                "技术", "Vue,Spring Boot,Docker", true));

        articleRepository.save(createArticle(
                "Docker 容器化部署最佳实践",
                "## 容器化部署的优势\n\n容器化技术已经成为现代应用部署的标准方式。\n\n## 核心原则\n\n1. **单一职责**：每个容器只运行一个进程\n2. **不可变基础设施**：容器创建后不修改\n3. **健康检查**：确保服务可用性\n\n```bash\ndocker compose up -d\ndocker compose ps\ndocker compose logs -f\n```",
                "技术", "Docker,DevOps", false));

        articleRepository.save(createArticle(
                "Vue 3 组合式 API 入门",
                "## 为什么选择组合式 API？\n\nVue 3 的组合式 API 提供了更灵活的代码组织方式。\n\n## 基础示例\n\n```javascript\nimport { ref, computed, onMounted } from 'vue'\n\nconst count = ref(0)\nconst double = computed(() => count.value * 2)\n\nonMounted(() => {\n  console.log('Component mounted!')\n})\n```",
                "技术", "Vue,JavaScript,前端", false));
    }

    private Article createArticle(String title, String content, String category, String tags, boolean pinned) {
        Article article = new Article();
        article.setTitle(title);
        article.setContent(content);
        article.setCategory(category);
        article.setTags(tags);
        article.setPinned(pinned);
        return article;
    }
}
