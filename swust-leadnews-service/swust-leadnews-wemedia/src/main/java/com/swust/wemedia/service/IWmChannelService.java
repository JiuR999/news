package com.swust.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swust.model.common.dtos.ResponseResult;
import com.swust.model.wemedia.pojos.WmChannel;

public interface IWmChannelService extends IService<WmChannel> {
    ResponseResult findAll();
}
