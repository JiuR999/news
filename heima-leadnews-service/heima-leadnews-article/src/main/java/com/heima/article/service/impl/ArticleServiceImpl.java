package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ArticleService;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleChannel;
import com.heima.model.article.pojos.EmpVO;
import com.heima.model.article.pojos.TestDep;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ArticleService {

    public static final int MAX_PAGE_SIZE = 50;

    public IPage<ApArticle> page(ArticleHomeDto dto) {
        if (!Optional.ofNullable(dto).isPresent()) {
            dto = new ArticleHomeDto();
        }
        if(dto.getPage() == null || dto.getSize() == null) {
            dto.ifAbsent();
        }
        LambdaQueryWrapper<ApArticle> wrapper = new LambdaQueryWrapper<ApArticle>()
                .eq(dto.getTag()!=null,ApArticle::getChannelId, dto.getTag());

        IPage<ApArticle> records = new Page<>(dto.getPage(), dto.getSize());
        return this.page(records, wrapper);
    }

    @Override
    public List<ApArticle> loadArticle(ArticleHomeDto articleHomeDto, Short type) {
        Integer size = articleHomeDto.getSize();
        if (size == null || size == 0) {
            size = 10;
        }
        size = Math.max(size, MAX_PAGE_SIZE);

        if (!type.equals(ArticleConstants.LOADTYPE_LOAD_MORE)
                && !type.equals(ArticleConstants.LOADTYPE_LOAD_NEW)) {
            type = ArticleConstants.LOADTYPE_LOAD_MORE;
        }

        //检验文章所属频道
        if (StringUtils.isBlank(articleHomeDto.getTag())) {
            articleHomeDto.setTag(ArticleConstants.DEFAULT_TAG);
        }

        if (articleHomeDto.getMaxBehotTime() == null) {
            articleHomeDto.setMaxBehotTime(new Date());
        }

        if (articleHomeDto.getMinBehotTime() == null) {
            articleHomeDto.setMinBehotTime(new Date());
        }
        List<ApArticle> apArticles = baseMapper.loadArticleList(articleHomeDto, type);

        return apArticles;
    }

    @Override
    public List<ApArticleChannel> getArticleTypes() {
        LambdaQueryWrapper<ApArticle> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(ApArticle::getChannelName, ApArticle::getChannelId)
                .groupBy(ApArticle::getChannelName, ApArticle::getChannelId);
        List<Map<String, Object>> list = baseMapper.selectMaps(queryWrapper);
        ArrayList<ApArticleChannel> channels = new ArrayList<>();
        for (Map<String, Object> item : list) {
            channels.add(ApArticleChannel.builder()
                    .channelName((String) item.get("channel_name"))
                    .channelId((Long) item.get("channel_id"))
                    .build()
            );
        }
        return channels;
    }

    @Override
    public List<EmpVO> getDep() {
        return baseMapper.getDep();
    }

/*    @Override
    public List<ApArticle> getByChannel(Long id) {
        return baseMapper.selectList();
    }*/
}
