package com.swust.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swust.model.common.dtos.IdsDto;
import com.swust.model.common.dtos.ResponseResult;
import com.swust.model.wemedia.dtos.WmAuditDto;
import com.swust.model.wemedia.dtos.WmNewsDto;
import com.swust.model.wemedia.dtos.WmNewsQueryDto;
import com.swust.model.wemedia.pojos.WmNews;
import com.swust.model.wemedia.vos.WmNewVO;

public interface IWmNewsService extends IService<WmNews> {

    ResponseResult<WmNewVO> list(WmNewsQueryDto dto);

    ResponseResult submitNews(WmNewsDto dto);

    ResponseResult<WmNewVO> getById(Long id);

    ResponseResult deleteNewsById(Long id);

    ResponseResult audit(WmAuditDto dto);
//    void addNewsToTask(ArticleDto dto, LocalDateTime publishTime);


    ResponseResult deleteBatch(IdsDto ids);
}
