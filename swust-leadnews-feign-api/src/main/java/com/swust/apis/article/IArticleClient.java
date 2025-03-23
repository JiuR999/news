package com.swust.apis.article;

import com.swust.apis.article.fallback.IArticleClientFallback;
import com.swust.config.FeignRequestInterceptor;
import com.swust.model.article.dtos.ArticleDto;
import com.swust.model.common.dtos.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "swust-article",
        fallback = IArticleClientFallback.class)
public interface IArticleClient {

    @PostMapping("/api/v1/article/submit")
    ResponseResult saveArticle(@RequestBody ArticleDto dto);

    @GetMapping("/api/v1/article/getById/{id}")
    ResponseResult getArticleById(@PathVariable Long id);

    @GetMapping("/api/v1/article/deleteById/{id}")
    ResponseResult deleteArticleById(@PathVariable Long id);
}
