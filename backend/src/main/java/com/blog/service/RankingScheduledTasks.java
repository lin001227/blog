package com.blog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RankingScheduledTasks {

    private final LLMRankingFetchService rankingFetchService;

    /**
     * 每天凌晨 6:00 自动更新大模型排行榜
     */
    @Scheduled(cron = "0 0 6 * * ?")
    public void autoFetchRankings() {
        log.info("【定时任务】开始自动更新大模型排行榜...");
        boolean success = rankingFetchService.fetchAndUpdate();
        if (success) {
            log.info("【定时任务】排行榜更新成功");
        } else {
            log.warn("【定时任务】排行榜更新失败，使用现有数据");
        }
    }
}
