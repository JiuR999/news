package com.swust.article.schedule;

import com.swust.article.service.ArticleService;
import com.swust.common.constants.CacheConstants;
import com.swust.common.redis.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduleTask {

    @Autowired
    CacheService cacheService;
    @Autowired
    ArticleService articleService;

    /**
     * 同步今日阅读量进数据库
     */
    @Scheduled(cron = "0 0 */1 * * ?")
    public void syncViews() {
        System.out.println("同步文章浏览量");
        articleService.syncCacheValue2DB(CacheConstants.RankType.RANK_VIEW);
    }

    /**
     * 同步今日文章下载量进数据库
     */
    @Scheduled(cron = "*/30 * * * * ?")
    public void syncDownloads() {
        System.out.println("同步文章下载量");
        articleService.syncCacheValue2DB(CacheConstants.RankType.RANK_DOWNLOAD);
    }

    /**
     * 同步今日收藏量进数据库
     */
    @Scheduled(cron = "*/5 * * * * ?")
    public void syncCollections() {
        System.out.println("同步文章收藏量");
        articleService.syncCacheValue2DB(CacheConstants.RankType.RANK_COLLECTION);
    }
}
