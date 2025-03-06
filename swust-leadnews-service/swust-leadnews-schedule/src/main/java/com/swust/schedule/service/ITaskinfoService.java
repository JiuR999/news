package com.swust.schedule.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swust.model.schedule.pojos.Taskinfo;
import com.swust.model.schedule.dtos.Task;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Zhangxu
 * @since 2025-02-25
 */
public interface ITaskinfoService extends IService<Taskinfo> {
    long addTask(Task task);

    boolean cancelTask(Long taskId);

    Task poll(int type, int priority);
}
