package com.swust.wemedia.controller.v1;

import com.swust.model.common.dtos.IdsDto;
import com.swust.model.common.dtos.ResponseResult;
import com.swust.model.wemedia.dtos.WmAuditDto;
import com.swust.model.wemedia.dtos.WmNewsDto;
import com.swust.model.wemedia.dtos.WmNewsQueryDto;
import com.swust.model.wemedia.pojos.WmNews;
import com.swust.wemedia.service.IWmNewsService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public ResponseResult audit(@RequestBody WmAuditDto dto) {
        return wmNewsService.audit(dto);
    }

    @GetMapping("/getById/{id}")
    public ResponseResult getById(@PathVariable Long id) {
        return wmNewsService.getById(id);
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation("根据id删除文章")
    public ResponseResult deleteNews(@PathVariable Long id) {
        return wmNewsService.deleteNewsById(id);
    }

    @PostMapping("/deleteBatch")
    @ApiOperation("根据ids删除资料")
    public ResponseResult deleteBatch(@RequestBody IdsDto ids) {
        return ResponseResult.okResult(wmNewsService.deleteBatch(ids));
    }

    @PostMapping("/update")
    @ApiOperation("根据id更新文章")
    public ResponseResult updateNews(@RequestBody WmNewsDto dto) {
        WmNews entity = new WmNews();
        BeanUtils.copyProperties(dto, entity);
        return ResponseResult.okResult(wmNewsService.updateById(entity));
    }
}
