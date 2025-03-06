package com.swust.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swust.article.mapper.ApArticleContentMapper;
import com.swust.article.mapper.ApArticleMapper;
import com.swust.article.service.ArticleService;
import com.swust.common.constants.ArticleConstants;
import com.swust.common.constants.KafkaMessageConstants;
import com.swust.common.exception.CustomException;
import com.swust.model.article.dtos.ArticleDto;
import com.swust.model.article.dtos.ArticleHomeDto;
import com.swust.model.article.pojos.ApArticle;
import com.swust.model.article.pojos.ApArticleChannel;
import com.swust.model.article.pojos.ApArticleContent;
import com.swust.model.article.pojos.EmpVO;
import com.swust.model.article.vos.ApArticleVO;
import com.swust.model.common.enums.AppHttpCodeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ArticleService {

    public static final int MAX_PAGE_SIZE = 50;

    @Autowired
    ApArticleContentMapper contentMapper;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    public IPage<ApArticle> page(ArticleHomeDto dto) {
        if (!Optional.ofNullable(dto).isPresent()) {
            dto = new ArticleHomeDto();

        }
        if (dto.getPage() == null || dto.getSize() == null) {
            dto.ifAbsent();
        }
        IPage<ApArticle> records = new Page<>(dto.getPage(), dto.getSize());
        if (dto.getKeyWord() != null && !dto.getKeyWord().isEmpty()) {
            List<ApArticle> articles = this.baseMapper.pageByContent(dto, (dto.getPage() - 1) * dto.getSize());
            records.setRecords(articles);
            return records;
        }
        LambdaQueryWrapper<ApArticle> wrapper = new LambdaQueryWrapper<ApArticle>()
                .eq(dto.getTag() != null, ApArticle::getChannelId, dto.getTag());
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

    @Override
    @Transactional
    public Boolean submit(ArticleDto dto) {
        ApArticle apArticle = new ApArticle();
        apArticle.setAuthorId(dto.getAuthorId());
        apArticle.setAuthorName(dto.getAuthorName());
        apArticle.setTitle(dto.getTitle());
        apArticle.setChannelId(dto.getChannelId());
        apArticle.setChannelName(dto.getChannelName());
        apArticle.setPublishTime(LocalDateTime.now());
        apArticle.setSyncStatus(true);
//        apArticle.setImages(dto.getImages());
        //向文章实体表插入数据
        boolean saved1 = this.save(apArticle);

        //向文章内容表插入内容
        ApArticleContent content = new ApArticleContent();
        content.setArticleId(Long.valueOf(apArticle.getId()));
        content.setContent(dto.getContent());
        int saved2 = contentMapper.insert(content);
        //通知文章已经发布 返回发布的文章在中转站的id以及文章本身的id
        HashMap<String, String> map = new HashMap<>();
        map.put("id", String.valueOf(dto.getId()));
        map.put("articleId", apArticle.getId());
        String jsonString = JSON.toJSONString(map);
        kafkaTemplate.send(KafkaMessageConstants.WM_NEWS_PUBLISH_TOPIC, jsonString);
        return saved1;
    }

    @Override
    public ApArticleVO getByIdWithContent(Long id) {
        ApArticle article = this.getById(id);
        ApArticleVO articleVO = new ApArticleVO();
        BeanUtils.copyProperties(article, articleVO);
        try {
            ApArticleContent content = contentMapper.selectOne(new LambdaQueryWrapper<ApArticleContent>()
                    .eq(ApArticleContent::getArticleId, id));
            articleVO.setContent(content.getContent());
            return articleVO;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    @Transactional
    public Boolean deleteById(long id) {
        boolean res1 = this.removeById(id);
        QueryWrapper<ApArticleContent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("article_id", id);
        int result = this.contentMapper.delete(queryWrapper);
        boolean res3 = result >= 1;
        return res1 && res3;
    }

    //根据文章ID 下载内容
    @Override
    public void download(long id, HttpServletResponse response)  {
        if(id == 0) throw new CustomException(AppHttpCodeEnum.PARAM_INVALID);
        ApArticle article = this.getOne(new LambdaQueryWrapper<ApArticle>().select(ApArticle::getTitle).eq(ApArticle::getId, id));
        ApArticleContent content = contentMapper.selectOne(new LambdaQueryWrapper<ApArticleContent>()
                .eq(ApArticleContent::getArticleId, id));
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "inline; filename=" + article.getTitle()+".html");
        ServletOutputStream stream = null;
        try {
            stream = response.getOutputStream();
            stream.write(content.getContent().getBytes());
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

/*    @Override
    public List<ApArticle> getByChannel(Long id) {
        return baseMapper.selectList();
    }*/
}
