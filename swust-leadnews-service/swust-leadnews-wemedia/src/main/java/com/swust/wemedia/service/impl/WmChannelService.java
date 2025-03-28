package com.swust.wemedia.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swust.model.common.dtos.ResponseResult;
import com.swust.model.wemedia.pojos.WmChannel;
import com.swust.wemedia.mapper.WmChannelMapper;
import com.swust.wemedia.service.IWmChannelService;
import org.springframework.stereotype.Service;

@Service
public class WmChannelService extends ServiceImpl<WmChannelMapper, WmChannel> implements IWmChannelService {
    @Override
    public ResponseResult findAll() {
        return ResponseResult.okResult(list());
    }
}
