package com.swust.article.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swust.model.article.dtos.ArticleHomeDto;
import com.swust.model.article.pojos.ApArticle;
import com.swust.model.article.pojos.EmpVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApArticleMapper extends BaseMapper<ApArticle> {
    List<ApArticle> loadArticleList(ArticleHomeDto dto, Short type);

    List<EmpVO> getDep();

    List<ApArticle> pageByContent(@Param("dto") ArticleHomeDto dto, Integer offset);
}
