package com.swust.schedule.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.swust.common.constants.ScheduleConstants;
import com.swust.common.redis.CacheService;
import com.swust.model.schedule.dtos.Task;
import com.swust.model.schedule.pojos.Taskinfo;
import com.swust.model.schedule.pojos.TaskinfoLogs;
import com.swust.schedule.mapper.TaskinfoLogsMapper;
import com.swust.schedule.mapper.TaskinfoMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swust.schedule.service.ITaskinfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Zhangxu
 * @since 2025-02-25
 */
@Service
@Transactional
@Slf4j
public class TaskinfoServiceImpl extends ServiceImpl<TaskinfoMapper, Taskinfo> implements ITaskinfoService {

    public static final int WAITING_TIME = 5;
    @Autowired
    TaskinfoLogsMapper logsMapper;
    @Autowired
    CacheService cacheService;


    @Scheduled(cron = "0 */5 * * * ?")
    @PostConstruct
    public void reloadData() {
        clearCache();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        //查看小于未来5分钟的所有任务
        List<Taskinfo> allTasks = this.baseMapper.selectList(Wrappers.<Taskinfo>lambdaQuery().lt(Taskinfo::getExecuteTime, calendar.getTime()));
        if (allTasks != null && !allTasks.isEmpty()) {
            for (Taskinfo taskinfo : allTasks) {
                Task task = new Task();
                BeanUtils.copyProperties(taskinfo, task);
                task.setExecuteTime(taskinfo.getExecuteTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                addTaskToCache(task);
            }
            log.info("同步{}条数据库数据同步到缓存",allTasks.size());
        }
    }

    private void clearCache() {
        // 删除缓存中未来数据集合和当前消费者队列的所有key
        Set<String> futurekeys = cacheService.scan(ScheduleConstants.FUTURE + "*");// future_
        Set<String> topickeys = cacheService.scan(ScheduleConstants.TOPIC + "*");// topic_
        cacheService.delete(futurekeys);
        cacheService.delete(topickeys);
    }

    @Override
    public long addTask(Task task) {
        //存入数据库 去重
        boolean success = this.addTaskToDb(task);
        if (success) {
            addTaskToCache(task);
        }
        return task.getTaskId();
    }

    @Override
    public boolean cancelTask(Long taskId) {
        boolean flag = false;
        Task task = updateDb(taskId, ScheduleConstants.CANCELLED);
        if (task != null) {
            removeTaskFromCache(task);
            flag = true;
        }
        return flag;
    }

    @Override
    public Task poll(int type, int priority) {
        Task task = null;
        try {
            String key = type + "_" + priority;
            String task_json = cacheService.lRightPop(ScheduleConstants.TOPIC + key);
            if (StringUtils.isNotBlank(task_json)) {
                task = JSON.parseObject(task_json, Task.class);
                //更新数据库信息
                updateDb(task.getTaskId(), ScheduleConstants.EXECUTED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("poll task exception");
        }

        return task;
    }

    public boolean addTaskToDb(Task task) {
        boolean flag;
        Taskinfo taskinfo = new Taskinfo();
        taskinfo.setPriority(task.getPriority());
        taskinfo.setTaskType(task.getTaskType());
        taskinfo.setParameters(task.getParameters());
        taskinfo.setExecuteTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(task.getExecuteTime()), ZoneId.systemDefault()));
        flag = this.save(taskinfo);

        task.setTaskId(taskinfo.getTaskId());
        //插入任务日志
        TaskinfoLogs taskinfoLogs = new TaskinfoLogs();
        taskinfoLogs.setTaskId(task.getTaskId());
        taskinfoLogs.setTaskType(task.getTaskType());
        taskinfoLogs.setParameters(task.getParameters());
        taskinfoLogs.setExecuteTime(taskinfo.getExecuteTime());
        taskinfoLogs.setPriority(task.getPriority());
        taskinfoLogs.setStatus(ScheduleConstants.SCHEDULED);
        taskinfoLogs.setVersion(1);
        logsMapper.insert(taskinfoLogs);
        return flag;
    }

    /**
     * 删除任务，更新任务日志状态
     *
     * @param taskId
     * @param status
     * @return
     */
    private Task updateDb(long taskId, int status) {
        Task task = null;
        try {
            //删除任务
            this.removeById(taskId);

            TaskinfoLogs taskinfoLogs = logsMapper.selectById(taskId);
            taskinfoLogs.setStatus(status);
            logsMapper.updateById(taskinfoLogs);

            task = new Task();
            BeanUtils.copyProperties(taskinfoLogs, task);
            task.setExecuteTime(taskinfoLogs.getExecuteTime().
                    atZone(ZoneId.systemDefault()).
                    toInstant().toEpochMilli());
        } catch (Exception e) {
            log.error("task cancel exception taskid=" + taskId);
        }

        return task;

    }

    public void addTaskToCache(Task task) {
        String key = task.getTaskType() + "_" + task.getPriority();
        //预设执行时间
        LocalDateTime plusMinutes = LocalDateTime.now().plusMinutes(WAITING_TIME);
        long nextExcuteTime = plusMinutes.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        //判断当前时间与任务执行时间的插值 如果任务执行时间小于当前时间 则存入消费队列 否则放入缓存
        if (task.getExecuteTime() < System.currentTimeMillis()) {
            cacheService.lLeftPush(ScheduleConstants.TOPIC + key, JSON.toJSONString(task));
        } else if (task.getExecuteTime() < nextExcuteTime) {
            cacheService.zAdd(ScheduleConstants.FUTURE + key, JSON.toJSONString(task), task.getExecuteTime());
        }
    }

    /**
     * 删除redis中的任务数据
     *
     * @param task
     */
    private void removeTaskFromCache(Task task) {

        String key = task.getTaskType() + "_" + task.getPriority();

        if (task.getExecuteTime() <= System.currentTimeMillis()) {
            cacheService.lRemove(ScheduleConstants.TOPIC + key, 0, JSON.toJSONString(task));
        } else {
            cacheService.zRemove(ScheduleConstants.FUTURE + key, JSON.toJSONString(task));
        }
    }
}
