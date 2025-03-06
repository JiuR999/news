package com.swust.wemedia.controller.v1;

import com.swust.model.common.dtos.ResponseResult;
import com.swust.model.wemedia.dtos.WmNewAuditDto;
import com.swust.model.wemedia.dtos.WmNewsDto;
import com.swust.model.wemedia.dtos.WmNewsQueryDto;
import com.swust.wemedia.service.IWmNewsService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;

@RestController
@RequestMapping("/api/v1/news")
public class NewsController {

    @Autowired
    IWmNewsService wmNewsService;

    @PostMapping("/list")
    public ResponseResult list(@RequestBody WmNewsQueryDto dto) {
        return wmNewsService.list(dto);
    }

    @PostMapping("/submit")
    @ApiOperation("提交资料")
    public ResponseResult submitNews(@RequestBody WmNewsDto dto) {
        return wmNewsService.submitNews(dto);
    }

    @PostMapping("/audit")
    @ApiOperation("审核文章")
    public ResponseResult audit(@RequestBody WmNewAuditDto dto) {
        return wmNewsService.audit(dto);
    }

    @GetMapping("/getById/{id}")
    public ResponseResult getById(@PathVariable Long id) {
        return wmNewsService.getById(id);
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation("根据id删除文章")
    public ResponseResult deleteNews(@PathVariable Long id) {
        return ResponseResult.okResult(wmNewsService.deleteNewsById(id));
    }
}
