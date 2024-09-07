package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsQueryDto;
import com.heima.model.wemedia.pojos.WmNews;

public interface IWmNewsService extends IService<WmNews> {

    public ResponseResult<WmNews> list(WmNewsQueryDto dto);

    ResponseResult submitNews(WmNewsDto dto);
}
