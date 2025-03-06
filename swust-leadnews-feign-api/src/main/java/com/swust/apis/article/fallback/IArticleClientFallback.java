package com.swust.apis.article.fallback;

import com.swust.apis.article.IArticleClient;
import com.swust.model.article.dtos.ArticleDto;
import com.swust.model.common.dtos.ResponseResult;
import com.swust.model.common.enums.AppHttpCodeEnum;
import org.springframework.stereotype.Component;

@Component
public class IArticleClientFallback implements IArticleClient {
    @Override
    public ResponseResult saveArticle(ArticleDto dto) {
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"获取数据失败");
    }

    @Override
    public ResponseResult getArticleById(Long id) {
        return null;
    }

    @Override
    public ResponseResult deleteArticleById(Long id) {
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"删除数据失败");
    }

}
