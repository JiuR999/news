package com.swust.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swust.apis.article.IArticleClient;
import com.swust.apis.schedule.IScheduleClient;
import com.swust.model.article.dtos.ArticleDto;
import com.swust.model.common.dtos.IdsDto;
import com.swust.model.common.dtos.PageResponseResult;
import com.swust.model.common.dtos.ResponseResult;
import com.swust.model.common.enums.AppHttpCodeEnum;
import com.swust.model.common.enums.TaskTypeEnum;
import com.swust.model.wemedia.dtos.WmAuditDto;
import com.swust.model.wemedia.dtos.WmNewsDto;
import com.swust.model.wemedia.dtos.WmNewsQueryDto;
import com.swust.model.wemedia.pojos.WmNews;
import com.swust.model.wemedia.pojos.WmUser;
import com.swust.model.wemedia.vos.WmNewVO;
import com.swust.model.schedule.dtos.Task;
import com.swust.utils.common.ProtostuffUtil;
import com.swust.utils.common.WmThreadLocalUtil;
import com.swust.wemedia.mapper.WmNewsMapper;
import com.swust.wemedia.service.IWmNewsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class WmNewsService extends ServiceImpl<WmNewsMapper, WmNews> implements IWmNewsService {
    @Autowired
    IArticleClient articleClient;

    @Autowired
    IScheduleClient scheduleClient;

    @Override
    public ResponseResult list(WmNewsQueryDto dto) {
        if (dto == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        WmUser user = WmThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        dto.checkParam();
//        IPage<WmNewVO> page = new Page<>(dto.getPage(), dto.getSize());
        PageResponseResult res = new PageResponseResult(dto.getPage(), dto.getSize());

        List<WmNewVO> wmNewVOS = this.baseMapper.list(dto, (dto.getPage() - 1) * dto.getSize(), dto.getSize());

//        LambdaQueryWrapper<WmNews> lambdaQueryWrapper = new LambdaQueryWrapper<>();
//        lambdaQueryWrapper.
//                eq(dto.getStatus() != null, WmNews::getStatus, dto.getStatus()).
//                eq(dto.getChannelId() != null, WmNews::getChannelId, dto.getChannelId()).
//                gt(dto.getBeginPubDate() != null, WmNews::getPublishTime, dto.getBeginPubDate()).
//                lt(dto.getEndPubDate() != null, WmNews::getPublishTime, dto.getEndPubDate());
//        if (StringUtils.isNoneBlank(dto.getKeyword())) {
//            lambdaQueryWrapper.like(WmNews::getTitle, dto.getKeyword());
//        }
//        lambdaQueryWrapper.orderByDesc(WmNews::getCreatedTime);
//        page = page(page, lambdaQueryWrapper);
        res.setTotal(wmNewVOS.size());
        res.setData(wmNewVOS);
        return res;
    }

    @Override
    public ResponseResult submitNews(WmNewsDto dto) {
        if (dto == null || dto.getContent() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //前端传递状态
        WmUser user = WmThreadLocalUtil.getUser();
        WmNews wmNews = new WmNews();
        BeanUtils.copyProperties(dto, wmNews);
        //设置作者信息
        wmNews.setUserId(user.getId());
        if (dto.getStatus() != 0) {
            wmNews.setSubmitedTime(LocalDateTime.now());
        }
        boolean saved = this.saveOrUpdate(wmNews);
        //提交到审核队列
        if (saved) {
            //加入队列 随机时间执行
            addNewsToTask(wmNews, TaskTypeEnum.NEWS_AUDIT_TIME, LocalDateTime.now().plusSeconds(30));
        }
        return ResponseResult.okResult(saved);
    }

    @Override
    public ResponseResult getById(Long id) {
        WmNewVO news = this.baseMapper.selectWithAuthorById(id);
        return ResponseResult.okResult(news);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ResponseResult deleteNewsById(Long id) {
        WmNewVO wmNewVO = this.baseMapper.selectWithAuthorById(id);
        //先删除中转站表里面的数据
        boolean res1 = this.removeById(id);
        boolean res2 = true;
        //如果已经上架 发消息给文章表下架文章
        if (wmNewVO.getStatus() == 9 && wmNewVO.getArticleId() != null) {
            Object data = articleClient.deleteArticleById(wmNewVO.getArticleId()).getData();
            res2 = (boolean) data;
        }
        return ResponseResult.okResult(res1 && res2);
    }

    @Override
    @Transactional
    public ResponseResult audit(WmAuditDto dto) {
        if (dto.getId() == null || dto.getId() == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //如果status = 3 说明机器审核不通过 转人工审核
        Short status;
        if (dto.getStatus() == 3) {
            status = dto.getStatus();
        } else {
            status = (short) (dto.getStatus() == 1 ? 8 : 2);
        }
        //修改wm数据库 更新状态
        //如果审核不通过 加上拒绝理由
        UpdateWrapper<WmNews> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("status", status)
                .set(dto.getReason() != null, "reason", dto.getReason())
                .eq(dto.getId() != null, "id", dto.getId());
        boolean updated = this.update(updateWrapper);
        //审核拒绝
        if (dto.getStatus() == 0 || dto.getStatus() == 3) {
            return ResponseResult.okResult("审核失败！");
        }
        //判断当前状态是否已经审核

        //如果审核通过 将此文章需同步至article表 使用延迟队列
        ResponseResult news = this.getById(dto.getId());
        WmNewVO wmNewVO = (WmNewVO) news.getData();
        ArticleDto articleDto = new ArticleDto();
        articleDto.setId(dto.getId());
        articleDto.setTitle(wmNewVO.getTitle());
        articleDto.setAuthorName(wmNewVO.getAuthor());
        articleDto.setPublishTime(LocalDateTime.now());
        articleDto.setChannelId(Long.valueOf(wmNewVO.getChannelId()));
        articleDto.setAuthorId(Long.valueOf(wmNewVO.getUserId()));
        articleDto.setChannelName(wmNewVO.getChannelName());
        articleDto.setContent(wmNewVO.getContent());
        //增加发布任务
        addNewsToTask(articleDto, TaskTypeEnum.NEWS_PUBLISH_TIME, LocalDateTime.now().plusSeconds(1));
        return ResponseResult.okResult("审核成功,等待发布！");
    }

    /**
     * 添加任务到延迟队列中
     *
     * @param dto         文章数据
     * @param publishTime 发布的时间  可以做为任务的执行时间
     */
    @Async
    public <T> void addNewsToTask(T dto, TaskTypeEnum typeEnum, LocalDateTime publishTime) {

        log.info("添加任务到延迟服务中----begin");

        Task task = new Task();
        task.setExecuteTime(publishTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        task.setTaskType(typeEnum.getTaskType());
        task.setPriority(typeEnum.getPriority());

        task.setParameters(ProtostuffUtil.serialize(dto));

        scheduleClient.addTask(task);

        log.info("添加任务到延迟服务中----end");

    }

    @Override
    public ResponseResult deleteBatch(IdsDto ids) {
        if (ids.getIds() == null || ids.getIds().isEmpty()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        List<Long> errIds = new ArrayList<>();
        for (int i = 0; i < ids.getIds().size(); i++) {
            ResponseResult result = deleteNewsById(ids.getIds().get(i));
            if(!((boolean) result.getData())) {
                errIds.add(ids.getIds().get(i));
            }
        }
        if(!errIds.isEmpty()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SUCCESS, "删除失败id为：" + errIds);
        }
        return ResponseResult.okResult(true);
    }

}
