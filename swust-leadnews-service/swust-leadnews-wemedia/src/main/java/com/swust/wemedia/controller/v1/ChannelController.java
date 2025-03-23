package com.swust.wemedia.controller.v1;

import com.swust.model.common.dtos.ResponseResult;
import com.swust.model.wemedia.pojos.WmChannel;
import com.swust.wemedia.service.IWmChannelService;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/channel")
public class ChannelController {

    @Autowired
    IWmChannelService wmChannelService;

    @GetMapping("/list")
    @ApiOperation(value = "获取所有频道", notes = "查询并返回所有频道信息") // 添加接口方法描述
    public ResponseResult findAll(){
        return wmChannelService.findAll();
    }

    @PostMapping("/add")
    public ResponseResult add(@RequestBody WmChannel channel){
        return ResponseResult.okResult(wmChannelService.save(channel));
    }

    @PostMapping("/delete/{id}")
    public ResponseResult deleteById(@PathVariable Long id){
        return ResponseResult.okResult(wmChannelService.removeById(id));
    }

    @PostMapping("/update")
    public ResponseResult update(@RequestBody WmChannel channel){
        return ResponseResult.okResult(wmChannelService.updateById(channel));
    }
}
