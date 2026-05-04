-- 数据库初始化脚本（Docker MariaDB 首次启动时自动执行）

USE blog;

CREATE TABLE IF NOT EXISTS article (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(100) DEFAULT '',
    tags VARCHAR(500) DEFAULT '',
    pinned BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS admin_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    display_name VARCHAR(100) DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 默认管理员（密码: admin123）
INSERT INTO admin_user (username, password, display_name)
SELECT 'admin', '$2a$12$q8wgvY5tABE9x/ZUU0Fv/eLb2RQeEgq1gIm3R59VMPHDTLsF5h27G', '管理员'
WHERE NOT EXISTS (SELECT 1 FROM admin_user WHERE username = 'admin');

CREATE TABLE IF NOT EXISTS language_ranking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rank_order INT NOT NULL DEFAULT 0,
    language_name VARCHAR(100) NOT NULL,
    percentage DECIMAL(5,1) NOT NULL DEFAULT 0.0,
    trend VARCHAR(10) NOT NULL DEFAULT 'stable',
    color VARCHAR(20) DEFAULT '#60a5fa',
    icon_url VARCHAR(512) DEFAULT '',
    description VARCHAR(500) DEFAULT '',
    month VARCHAR(7) DEFAULT '',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO language_ranking (rank_order, language_name, percentage, trend, color, icon_url, description, month) VALUES
(1, 'Claude 4 Sonnet', 28.5, 'up', '#D97757', 'https://cdn.simpleicons.org/anthropic/D97757', 'Anthropic · 全能最强，编程与推理标杆', '2026-04'),
(2, 'GPT-5', 26.2, 'up', '#10A37F', 'https://cdn.simpleicons.org/openai/10A37F', 'OpenAI · 多模态与通用能力顶尖', '2026-04'),
(3, 'DeepSeek-R1', 21.8, 'up', '#4F46E5', 'https://cdn.simpleicons.org/deepseek/4F46E5', '深度求索 · 开源推理模型新王', '2026-04'),
(4, 'Gemini 2.5 Pro', 19.4, 'up', '#4285F4', 'https://cdn.simpleicons.org/google/4285F4', 'Google · 百万级上下文窗口', '2026-04'),
(5, 'Claude 4 Opus', 17.6, 'stable', '#C75B39', 'https://cdn.simpleicons.org/anthropic/C75B39', 'Anthropic · 深度推理旗舰', '2026-04'),
(6, 'Llama 4', 14.3, 'up', '#6B44A8', 'https://cdn.simpleicons.org/meta/6B44A8', 'Meta · MoE架构，开源生态核心', '2026-04'),
(7, 'Qwen3-235B', 12.1, 'up', '#1677FF', 'https://cdn.simpleicons.org/alibabacloud/1677FF', '阿里通义 · 中文最强开源模型', '2026-04'),
(8, 'DeepSeek-V3', 10.5, 'down', '#6366F1', 'https://cdn.simpleicons.org/deepseek/6366F1', '深度求索 · 全能型基础模型', '2026-04'),
(9, 'Mistral Large 3', 8.9, 'stable', '#F97316', 'https://cdn.simpleicons.org/mistral/F97316', 'Mistral AI · 欧洲最强，效率优先', '2026-04'),
(10, 'Grok 4', 7.2, 'up', '#1DA1F2', 'https://cdn.simpleicons.org/x/1DA1F2', 'xAI · 实时信息与幽默风格', '2026-04')
ON DUPLICATE KEY UPDATE language_name=VALUES(language_name);

CREATE TABLE IF NOT EXISTS subscriber (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS external_article (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    url VARCHAR(1024) NOT NULL,
    title VARCHAR(255) NOT NULL,
    source VARCHAR(100) DEFAULT '',
    cover_url VARCHAR(512) DEFAULT '',
    summary TEXT,
    original_content MEDIUMTEXT,
    summary_type VARCHAR(20) DEFAULT 'ai',
    category VARCHAR(50) DEFAULT '',
    tags VARCHAR(255) DEFAULT '',
    fetched_at DATETIME,
    published_at DATETIME,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    error_msg VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 示例文章
INSERT INTO article (title, content, category, tags, pinned, created_at) VALUES
('从零搭建个人博客：我的技术栈选择', '这是博客的第一篇文章。很高兴能在这个新家与你相遇。\n\n## 为什么重写博客\n\n之前的博客用了 WordPress，虽然功能强大但总觉得太重了。这次决定从零写一个，轻量、可控、好玩。', '杂谈', '博客,Spring Boot,Vue', TRUE, '2026-04-30 10:00:00'),
('Docker 容器化部署最佳实践', '从多阶段构建到镜像瘦身，从 docker-compose 到健康检查——这套实践帮我省下了 60% 的部署时间。', 'Docker', 'Docker,DevOps', FALSE, '2026-04-28 14:30:00'),
('Vue 3 组合式 API 入门', 'Composition API 是 Vue 3 最核心的新特性之一。它让我们以更灵活的方式组织组件逻辑。\n\n## setup 函数\n\nsetup 是组合式 API 的入口。它在组件创建之前执行。', '前端', 'Vue,JavaScript', FALSE, '2026-04-25 09:00:00')
ON DUPLICATE KEY UPDATE title=VALUES(title);
