package com.swust.model.article.vos;

import com.swust.model.article.pojos.ApArticle;
import lombok.Data;

@Data
public class ApArticleVO extends ApArticle {
    /**
     * 作者名称
     */
    private String content;

    /**
     * 是否收藏
     */
    private Boolean isCollection;

}
