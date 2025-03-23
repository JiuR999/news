package com.swust.article.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swust.model.article.dtos.ArticleHomeDto;
import com.swust.model.article.dtos.ArticleSyncDto;
import com.swust.model.article.pojos.ApArticle;
import com.swust.model.article.pojos.EmpVO;
import com.swust.model.article.vos.ApArticleVO;
import com.swust.model.common.pojos.StatisticModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApArticleMapper extends BaseMapper<ApArticle> {
    List<ApArticle> loadArticleList(ArticleHomeDto dto, Short type);

    List<EmpVO> getDep();

    List<ApArticleVO> pageByContent(@Param("dto") ArticleHomeDto dto, Integer userId, Integer offset);

    /**
     * 批量增量更新
     * @param dto
     */
    void updateBatchData(ArticleSyncDto dto);

    /**
     * 更新文章可视化数据信息
     * @param dto
     */
    void updateViews(ArticleSyncDto dto);

    /**
     * 返回指定条件的数据条数
     * @param dto 查询条件
     * @return 数据条数
     */
    Integer count(@Param("dto") ArticleHomeDto dto);

    /**
     * 获取某人的收藏列表
     * @param userId 用户id
     * @return 收藏的文章列表数据
     */
    List<ApArticleVO> selectCollectionList(Integer userId);

    List<StatisticModel> selectArticleStatsByChannel();

    List<StatisticModel> selectArticleTop10Views();
}
