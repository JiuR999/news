package com.swust.schedule.task;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swust.apis.article.IArticleClient;
import com.swust.apis.schedule.IScheduleClient;
import com.swust.common.constants.CacheConstants;
import com.swust.common.constants.ScheduleConstants;
import com.swust.common.redis.CacheService;
import com.swust.model.article.dtos.ArticleDto;
import com.swust.model.common.dtos.ResponseResult;
import com.swust.model.common.enums.TaskTypeEnum;
import com.swust.model.schedule.dtos.Task;
import com.swust.model.schedule.pojos.Taskinfo;
import com.swust.schedule.mapper.TaskinfoMapper;
import com.swust.utils.common.ProtostuffUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Component
public class ScheduleTask {
    @Autowired
    CacheService cacheService;

    @Autowired
    IScheduleClient scheduleClient;

    @Autowired
    IArticleClient articleClient;

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

}
