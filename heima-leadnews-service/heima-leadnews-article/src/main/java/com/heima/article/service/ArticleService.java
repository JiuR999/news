package com.heima.article.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleChannel;
import com.heima.model.article.pojos.EmpVO;
import com.heima.model.article.pojos.TestDep;

import java.util.List;

public interface ArticleService extends IService<ApArticle> {
    /**
     *
     * @param articleHomeDto
     * @param type 1 加载更多 2 刷新
     * @return
     */
    List<ApArticle> loadArticle(ArticleHomeDto articleHomeDto,Short type);
    IPage<ApArticle> page(ArticleHomeDto dto);
    List<ApArticleChannel> getArticleTypes();

    List<EmpVO> getDep();

//    List<ApArticle> getByChannel(Long id);
}
