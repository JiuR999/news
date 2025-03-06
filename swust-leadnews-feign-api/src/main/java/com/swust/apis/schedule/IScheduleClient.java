package com.swust.apis.schedule;

import com.swust.model.common.dtos.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import com.swust.model.schedule.dtos.Task;

@FeignClient(value = "swust-schedule")
public interface IScheduleClient {

    @PostMapping("/api/v1/task/add")
    ResponseResult addTask(@RequestBody Task task);

    @DeleteMapping("/api/v1/task/cancel/{taskId}")
    ResponseResult cancelTask(@PathVariable Long taskId);

    /**
     * 按照类型和优先级来拉取任务
     *
     * @param type
     * @param priority
     * @return
     */
    @GetMapping("/api/v1/task/poll/{type}/{priority}")
    ResponseResult poll(@PathVariable("type") int type, @PathVariable("priority") int priority);
}
