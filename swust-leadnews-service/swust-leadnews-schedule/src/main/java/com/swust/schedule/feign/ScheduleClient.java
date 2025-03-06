package com.swust.schedule.feign;

import com.swust.apis.schedule.IScheduleClient;
import com.swust.model.common.dtos.ResponseResult;
import com.swust.model.schedule.dtos.Task;
import com.swust.schedule.service.ITaskinfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/task")
public class ScheduleClient implements IScheduleClient {
    @Autowired
    ITaskinfoService taskinfoService;
    @PostMapping("/add")
    @Override
    public ResponseResult addTask(@RequestBody Task task) {
        return ResponseResult.okResult(taskinfoService.addTask(task));
    }

    @DeleteMapping("/cancel/{taskId}")
    @Override
    public ResponseResult cancelTask(@PathVariable Long taskId) {
        return ResponseResult.okResult(taskinfoService.cancelTask(taskId));
    }

    @GetMapping("/poll/{type}/{priority}")
    @Override
    public ResponseResult poll(@PathVariable int type, @PathVariable int priority) {
        return ResponseResult.okResult(taskinfoService.poll(type, priority));
    }
}
