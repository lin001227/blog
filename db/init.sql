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

-- 示例文章
INSERT INTO article (title, content, category, tags, pinned, created_at) VALUES
('从零搭建个人博客：我的技术栈选择', '这是博客的第一篇文章。很高兴能在这个新家与你相遇。\n\n## 为什么重写博客\n\n之前的博客用了 WordPress，虽然功能强大但总觉得太重了。这次决定从零写一个，轻量、可控、好玩。', '杂谈', '博客,Spring Boot,Vue', TRUE, '2026-04-30 10:00:00'),
('Docker 容器化部署最佳实践', '从多阶段构建到镜像瘦身，从 docker-compose 到健康检查——这套实践帮我省下了 60% 的部署时间。', 'Docker', 'Docker,DevOps', FALSE, '2026-04-28 14:30:00'),
('Vue 3 组合式 API 入门', 'Composition API 是 Vue 3 最核心的新特性之一。它让我们以更灵活的方式组织组件逻辑。\n\n## setup 函数\n\nsetup 是组合式 API 的入口。它在组件创建之前执行。', '前端', 'Vue,JavaScript', FALSE, '2026-04-25 09:00:00')
ON DUPLICATE KEY UPDATE title=VALUES(title);
