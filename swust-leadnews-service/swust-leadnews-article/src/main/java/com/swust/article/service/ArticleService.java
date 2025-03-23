package com.swust.article.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.swust.common.constants.CacheConstants;
import com.swust.model.article.dtos.ArticleDto;
import com.swust.model.article.dtos.ArticleHomeDto;
import com.swust.model.article.pojos.ApArticle;
import com.swust.model.article.pojos.ApArticleChannel;
import com.swust.model.article.pojos.EmpVO;
import com.swust.model.article.vos.ApArticleVO;
import com.swust.model.common.dtos.IdsDto;
import com.swust.model.common.pojos.StatisticModel;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface ArticleService extends IService<ApArticle> {
    /**
     *
     * @param articleHomeDto
     * @param type 1 加载更多 2 刷新
     * @return
     */
    List<ApArticle> loadArticle(ArticleHomeDto articleHomeDto,Short type);
    IPage<ApArticleVO> page(ArticleHomeDto dto);
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

    boolean addViews(String userInfo, Long articleId);

    boolean syncCacheValue2DB(CacheConstants.RankType rankType);

    /**
     * 收藏文章
     * @param articleId 收藏文章id
     * @return
     */
    boolean collect(Long articleId);

    boolean cancelCollect(Long articleId);

    List<ApArticleVO> myCollections();

    List<StatisticModel> getArticleStatsByChannel();

    List<StatisticModel> getArticleTop10Views();

    boolean deleteBatch(IdsDto ids);
//    List<ApArticle> getByChannel(Long id);
}
