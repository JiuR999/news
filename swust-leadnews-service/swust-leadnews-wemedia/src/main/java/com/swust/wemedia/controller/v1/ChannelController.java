package com.swust.wemedia.controller.v1;

import com.swust.model.common.dtos.ResponseResult;
import com.swust.wemedia.service.IWmChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/channel")
public class ChannelController {

    @Autowired
    IWmChannelService wmChannelService;

    @GetMapping("/list")
    public ResponseResult findAll(){
        return wmChannelService.findAll();
    }
}
