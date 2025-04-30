package com.swust.schedule.task;

import com.alibaba.fastjson.JSON;
import com.swust.apis.article.IArticleClient;
import com.swust.apis.schedule.IScheduleClient;
import com.swust.apis.wemdia.IWmNewsClient;
import com.swust.common.constants.ScheduleConstants;
import com.swust.common.redis.CacheService;
import com.swust.model.article.dtos.ArticleDto;
import com.swust.model.common.dtos.ResponseResult;
import com.swust.model.common.enums.TaskTypeEnum;
import com.swust.model.schedule.dtos.Task;
import com.swust.model.wemedia.dtos.WmAuditDto;
import com.swust.model.wemedia.dtos.WmMaterialSearchDto;
import com.swust.model.wemedia.pojos.WmNews;
import com.swust.utils.common.ProtostuffUtil;
import com.swust.utils.common.SensitiveWordUtil;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Set;

@Component
public class ScheduleTask {
    @Autowired
    CacheService cacheService;

    @Autowired
    IScheduleClient scheduleClient;

    @Autowired
    IArticleClient articleClient;

    @Autowired
    IWmNewsClient wmNewsClient;

    @Autowired
    RestHighLevelClient restHighLevelClient;
    /**
     * 定时刷新任务队列
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void refresh() {
        // 获取所有未来数据集合的key值
        Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");// future_*
        for (String futureKey : futureKeys) { // future_250_250

            String topicKey = ScheduleConstants.TOPIC + futureKey.split(ScheduleConstants.FUTURE)[1];
            //获取该组key下当前需要消费的任务数据
            Set<String> tasks = cacheService.zRangeByScore(futureKey, 0, System.currentTimeMillis());
            if (!tasks.isEmpty()) {
                //将这些任务数据添加到消费者队列中
                cacheService.refreshWithPipeline(futureKey, topicKey, tasks);
                System.out.println("成功的将" + futureKey + "下的当前需要执行的任务数据刷新到" + topicKey + "下");
            }
        }
    }

    /**
     * 定时发布资料任务
     * 定时从延迟任务队列拉取
     */
    @Scheduled(fixedRate = 8000)
    public void publish() {
        ResponseResult responseResult = scheduleClient.poll(TaskTypeEnum.NEWS_PUBLISH_TIME.getTaskType(), TaskTypeEnum.NEWS_PUBLISH_TIME.getPriority());
        if (responseResult.getCode().equals(200) && responseResult.getData() != null) {
            String json_str = JSON.toJSONString(responseResult.getData());
            Task task = JSON.parseObject(json_str, Task.class);
            byte[] parameters = task.getParameters();

            ArticleDto dto = ProtostuffUtil.deserialize(parameters, ArticleDto.class);
            System.out.println(dto.getTitle() + "-----------" + "准备发布");
            //可能发生异常 续时
            try {
                ResponseResult result = articleClient.saveArticle(dto);
                boolean published = (boolean) result.getData();
                if (!published) {
                    System.err.println(dto.getTitle() + "-----------" + "发布失败");
                }
            } catch (Exception e) {
                e.printStackTrace();
                task.setExecuteTime(LocalDateTime.now().plusMinutes(5).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                ResponseResult result = scheduleClient.addTask(task);
                System.out.println(result.getData());
                if (result.getCode().equals(200) && result.getData() != null) {
                    System.err.println(dto.getTitle() + "-----------" + "发布失败,稍后重新发布！");
                }
            }
        }
    }

    /**
     * 定时审核资料任务
     * 定时从延迟任务队列拉取
     */
    @Scheduled(fixedRate = 5000)
    public void audit() {
        ResponseResult responseResult = scheduleClient.poll(TaskTypeEnum.NEWS_AUDIT_TIME.getTaskType(), TaskTypeEnum.NEWS_AUDIT_TIME.getPriority());
        if (responseResult.getCode().equals(200) && responseResult.getData() != null) {
            String json_str = JSON.toJSONString(responseResult.getData());
            Task task = JSON.parseObject(json_str, Task.class);
            byte[] parameters = task.getParameters();
            //序列化资料类型
            WmNews news = ProtostuffUtil.deserialize(parameters, WmNews.class);
            System.out.println(news.getTitle() + "-----------" + "准备审核");
            WmAuditDto newsDto = new WmAuditDto();
            newsDto.setId(Long.valueOf(news.getId()));
            //可能发生异常 续时
            try {
                //调用审核方法 自动审核
                System.out.println("审核：" + news.getContent());
                Map<String, Integer> matchWords = SensitiveWordUtil.matchWords(news.getContent());
                if (!matchWords.isEmpty()) {
                    System.out.println("匹配到敏感词" + matchWords);
                    //审核不通过 调用更新 回传不通过理由
                    newsDto.setStatus((short) 3);
                    newsDto.setReason("文章包含敏感词:" + matchWords);
                } else {
                    //审核通过 调用更新
                    newsDto.setStatus((short) 1);
                }
                ResponseResult result = wmNewsClient.audit(newsDto);

                String audited = (String) result.getData();
                if (audited.contains("审核失败")) {
                    System.err.println(news.getTitle() + "-----------" + "审核失败");
                }
            } catch (Exception e) {
                e.printStackTrace();
                task.setExecuteTime(LocalDateTime.now().plusMinutes(5).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                ResponseResult result = scheduleClient.addTask(task);
                System.out.println(result.getData());
                if (result.getCode().equals(200) && result.getData() != null) {
                    System.err.println(news.getTitle() + "-----------" + "审核失败,稍后人工审核！");
                }
            }
        }
    }

    /**
     * 定时审核文件资料任务
     * 定时从延迟任务队列拉取
     */
    @Scheduled(fixedRate = 5000)
    public void auditFile() {
        ResponseResult responseResult = scheduleClient.poll(TaskTypeEnum.FILE_AUDIT_TIME.getTaskType(), TaskTypeEnum.FILE_AUDIT_TIME.getPriority());
        if (responseResult.getCode().equals(200) && responseResult.getData() != null) {
            String json_str = JSON.toJSONString(responseResult.getData());
            Task task = JSON.parseObject(json_str, Task.class);
            byte[] parameters = task.getParameters();
            //序列化资料类型
            WmMaterialSearchDto dto = ProtostuffUtil.deserialize(parameters, WmMaterialSearchDto.class);
            System.out.println(dto.getFileName() + "-----------" + "准备审核");

            WmAuditDto newsDto = new WmAuditDto();
            newsDto.setId(Long.valueOf(dto.getId()));
            //可能发生异常 续时
            try {
                //调用审核方法 自动审核
                Map<String, Integer> matchWords = SensitiveWordUtil.matchWords(dto.getFileContent());
                if (!matchWords.isEmpty()) {
                    System.out.println("匹配到敏感词" + matchWords);
                    //审核不通过 调用更新 回传不通过理由
                    newsDto.setStatus((short) 3);
                    newsDto.setReason("文章包含敏感词:" + matchWords);
                } else {
                    //审核通过 调用更新
                    newsDto.setStatus((short) 1);
                }
                ResponseResult result = wmNewsClient.audit(newsDto);

                String audited = (String) result.getData();
                if (audited.contains("审核失败")) {
                    System.err.println(dto.getFileContent() + "-----------" + "审核失败");
                } else {
                    //存入Es

                }
            } catch (Exception e) {
                e.printStackTrace();
                task.setExecuteTime(LocalDateTime.now().plusMinutes(5).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                ResponseResult result = scheduleClient.addTask(task);
                System.out.println(result.getData());
                if (result.getCode().equals(200) && result.getData() != null) {
                    System.err.println(dto.getFileName() + "-----------" + "审核失败,稍后人工审核！");
                }
            }
        }
    }

}
