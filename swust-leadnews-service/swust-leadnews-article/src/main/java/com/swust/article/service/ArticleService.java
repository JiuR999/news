package com.swust.article.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.swust.model.article.dtos.ArticleDto;
import com.swust.model.article.dtos.ArticleHomeDto;
import com.swust.model.article.pojos.ApArticle;
import com.swust.model.article.pojos.ApArticleChannel;
import com.swust.model.article.pojos.EmpVO;
import com.swust.model.article.vos.ApArticleVO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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

    /**
     * 发布文章
     * @param dto 文章实体
     * @return
     */
    Boolean submit(ArticleDto dto);

    ApArticleVO getByIdWithContent(Long id);

    Boolean deleteById(long id);

    void download(long id, HttpServletResponse response);
//    List<ApArticle> getByChannel(Long id);
}
