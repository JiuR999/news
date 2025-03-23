package com.swust.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swust.apis.wemdia.IWmNewsClient;
import com.swust.article.mapper.ApArticleContentMapper;
import com.swust.article.mapper.ApArticleMapper;
import com.swust.article.mapper.ApCollectionMapper;
import com.swust.article.service.ArticleService;
import com.swust.common.constants.ArticleConstants;
import com.swust.common.constants.CacheConstants;
import com.swust.common.constants.KafkaMessageConstants;
import com.swust.common.exception.CustomException;
import com.swust.common.redis.CacheService;
import com.swust.model.article.dtos.ArticleDto;
import com.swust.model.article.dtos.ArticleHomeDto;
import com.swust.model.article.dtos.ArticleSyncDto;
import com.swust.model.article.pojos.*;
import com.swust.model.article.vos.ApArticleVO;
import com.swust.model.common.dtos.IdsDto;
import com.swust.model.common.enums.AppHttpCodeEnum;
import com.swust.model.common.pojos.StatisticModel;
import com.swust.model.wemedia.dtos.WmNewsDto;
import com.swust.model.wemedia.pojos.WmUser;
import com.swust.utils.common.WmThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class ArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ArticleService {

    public static final int MAX_PAGE_SIZE = 50;

    @Autowired
    ApArticleContentMapper contentMapper;

    @Autowired
    ApCollectionMapper apCollectionMapper;

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    CacheService cacheService;

    @Autowired
    IWmNewsClient wmNewsClient;
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    public IPage<ApArticleVO> page(ArticleHomeDto dto) {
        if (!Optional.ofNullable(dto).isPresent()) {
            dto = new ArticleHomeDto();

        }
        if (dto.getPage() == null || dto.getSize() == null) {
            dto.ifAbsent();
        }
        IPage<ApArticleVO> records = new Page<>(dto.getPage(), dto.getSize());
        //搜索模式
//        if (dto.getKeyWord() != null && !dto.getKeyWord().isEmpty()) {
        WmUser wmUser = WmThreadLocalUtil.getUser();
        if (wmUser == null) {
            throw new CustomException(AppHttpCodeEnum.NEED_LOGIN);
        }
        Integer total = this.baseMapper.count(dto);
        List<ApArticleVO> articles = this.baseMapper.pageByContent(dto, wmUser.getId(), (dto.getPage() - 1) * dto.getSize());
        records.setTotal(total);
        records.setRecords(articles);
        return records;
//        }
/*        LambdaQueryWrapper<ApArticleVO> wrapper = new LambdaQueryWrapper<ApArticleVO>()
                .eq(dto.getTag() != null, ApArticle::getChannelId, dto.getTag());
        return this.page(records, wrapper);*/
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
        try {
            //事务报错 则将文章回退至日志 设置状态
            kafkaTemplate.send(KafkaMessageConstants.WM_NEWS_PUBLISH_TOPIC, jsonString);
        } catch (Exception e) {
            //发送消息失败 根据ID 更新信息 将状态变更为-1（发布失败）
            log.info("发布文章失败{}", e.getCause());
            // 手动标记事务为回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            WmNewsDto wmNewsDto = new WmNewsDto();
            wmNewsDto.setId(Math.toIntExact(dto.getId()));
            wmNewsDto.setStatus((short) -1);
            wmNewsClient.updateById(wmNewsDto);
            e.printStackTrace();
            return false;
        }
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
    public void download(long id, HttpServletResponse response) {
        if (id == 0) throw new CustomException(AppHttpCodeEnum.PARAM_INVALID);
        //加入队列 增加下载量
        //向Redis增加浏览量--异步
        ArticleSyncDto syncDto = new ArticleSyncDto();
        syncDto.setId(id);
        syncDto.setColumn("downloads");
        incrementCountAsync(CacheConstants.CACHE_ARTICLE_DOWNLOAD_KEY, syncDto);
        //获填充文章下载内容
        ApArticle article = this.getOne(new LambdaQueryWrapper<ApArticle>().select(ApArticle::getTitle).eq(ApArticle::getId, id));
        ApArticleContent content = contentMapper.selectOne(new LambdaQueryWrapper<ApArticleContent>()
                .eq(ApArticleContent::getArticleId, id));
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "inline; filename=" + article.getTitle() + ".html");
        ServletOutputStream stream = null;
        try {
            stream = response.getOutputStream();
            stream.write(content.getContent().getBytes());
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean addViews(String userInfo, Long articleId) {
        if (StringUtils.isBlank(userInfo)) {
            return false;
        }
        JSONObject jsonObject = JSON.parseObject(userInfo);
//        {"name":"admin","id":4}
        //获取用户id
        //向Redis增加浏览量--异步
        ArticleSyncDto syncDto = new ArticleSyncDto();
        syncDto.setId(articleId);
        syncDto.setColumn("views");
        incrementCountAsync(CacheConstants.CACHE_ARTICLE_VIEW_KEY, syncDto);
        return true;
    }

    @Override
    public boolean collect(Long articleId) {
        WmUser user = WmThreadLocalUtil.getUser();
        if (user == null) {
            throw new CustomException(AppHttpCodeEnum.NEED_LOGIN);
        }
        ApCollection apCollection = new ApCollection();
        apCollection.setCollectionTime(LocalDateTime.now());
        apCollection.setArticleId(articleId);
        apCollection.setUserId(user.getId());
        //增加收藏数加一
        cacheService.zIncrementScore(CacheConstants.CACHE_ARTICLE_COLLECTION_KEY, String.valueOf(articleId), 1);
        //在文章收藏表中增加关系
        return apCollectionMapper.insert(apCollection) > 0;
    }

    @Override
    public boolean cancelCollect(Long articleId) {
        WmUser user = WmThreadLocalUtil.getUser();
        if (user == null) {
            throw new CustomException(AppHttpCodeEnum.NEED_LOGIN);
        }

        //如果当前收藏数为0 则移除 否则减一
        Double v = cacheService.zScore(CacheConstants.CACHE_ARTICLE_VIEW_KEY, String.valueOf(articleId));
        if (v != null) {
            if (v == 0.0) {
                cacheService.zRemove(CacheConstants.CACHE_ARTICLE_VIEW_KEY, String.valueOf(articleId));
            } else {
                cacheService.zIncrementScore(CacheConstants.CACHE_ARTICLE_COLLECTION_KEY, String.valueOf(articleId), -1);
            }
        }
        //在文章收藏表中删除关系
        QueryWrapper<ApCollection> wrapper = new QueryWrapper<ApCollection>()
                .eq("user_id", user.getId())
                .eq("article_id", articleId);
        return apCollectionMapper.delete(wrapper) >= 1;

    }

    @Override
    public List<ApArticleVO> myCollections() {
        WmUser user = WmThreadLocalUtil.getUser();
        if (user == null) {
            throw new CustomException(AppHttpCodeEnum.NEED_LOGIN);
        }
        List<ApArticleVO> apArticleVOS = this.baseMapper.selectCollectionList(user.getId());
        return apArticleVOS;
    }

    @Async
    @Transactional
    public void incrementCountAsync(String cacheKey, ArticleSyncDto dto) {
        try {
            System.out.println(cacheKey);
            cacheService.zIncrementScore(cacheKey, String.valueOf(dto.getId()), 1);
            Double views = cacheService.zScore(cacheKey, String.valueOf(dto.getId()));
            dto.setValue(views.longValue());
            this.baseMapper.updateViews(dto);
        } catch (Exception e) {
            // 记录日志或进行其他错误处理
            e.printStackTrace();
        }
    }

    @Override
    @Transactional
    public boolean syncCacheValue2DB(CacheConstants.RankType rankType) {
        ArrayList<ArticleSyncDto> dtos = new ArrayList<>();
        SqlSession sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH);
        ApArticleMapper mapper = sqlSession.getMapper(ApArticleMapper.class);
        String key = CacheConstants.CACHE_ARTICLE_RANK_KEY_PREFIX + rankType.getType();
        Set<ZSetOperations.TypedTuple<String>> typedTuples = cacheService.zRangeWithScores(key, 0, -1);
        Iterator<ZSetOperations.TypedTuple<String>> iterator = typedTuples.iterator();
        try {
            while (iterator.hasNext()) {
                ArticleSyncDto syncDto = new ArticleSyncDto();
                ZSetOperations.TypedTuple<String> item = iterator.next();
                syncDto.setColumn(rankType.getType());
                syncDto.setValue(Objects.requireNonNull(item.getScore()).longValue());
                syncDto.setId(Long.valueOf(Objects.requireNonNull(item.getValue())));
                mapper.updateBatchData(syncDto);
                dtos.add(syncDto);
            }
            sqlSession.commit();
        } catch (Exception e) {
            sqlSession.rollback();
            throw e;
        } finally {
            sqlSession.close();
        }
        cacheService.delete(key);
        return true;
    }

    @Override
    public List<StatisticModel> getArticleStatsByChannel() {
        return this.baseMapper.selectArticleStatsByChannel();
    }

    @Override
    public List<StatisticModel> getArticleTop10Views() {
        return this.baseMapper.selectArticleTop10Views();
    }

    @Override
    public boolean deleteBatch(IdsDto ids) {

        return false;
    }
/*    @Override
    public List<ApArticle> getByChannel(Long id) {
        return baseMapper.selectList();
    }*/

    @Override
    public List<EmpVO> getDep() {
        return baseMapper.getDep();
    }
}
