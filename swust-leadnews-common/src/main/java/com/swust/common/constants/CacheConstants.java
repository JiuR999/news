package com.swust.common.constants;

public class CacheConstants {
    /**
     * 文章可视化量排名缓存键。
     * 用于存储和获取文章的可视化量排名数据。
     */
    public final static String CACHE_ARTICLE_RANK_KEY_PREFIX = "rank:";

    /**
     * 文章浏览量排名缓存键。
     * 用于存储和获取文章的浏览量排名数据。
     */
    public final static String CACHE_ARTICLE_VIEW_KEY = "rank:" + RankType.RANK_VIEW.type;

    /**
     * 文章收藏量排名缓存键。
     * 用于存储和获取文章的收藏量排名数据。
     */
    public final static String CACHE_ARTICLE_COLLECTION_KEY = "rank:" + RankType.RANK_COLLECTION.type;

    /**
     * 文章下载量排名缓存键。
     * 用于存储和获取文章的下载量排名数据。
     */
    public final static String CACHE_ARTICLE_DOWNLOAD_KEY = "rank:" + RankType.RANK_DOWNLOAD.type;

    public enum RankType {
        RANK_VIEW("views"),
        RANK_COLLECTION("collection"),
        RANK_DOWNLOAD("downloads");
        String type;
        RankType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }
}


