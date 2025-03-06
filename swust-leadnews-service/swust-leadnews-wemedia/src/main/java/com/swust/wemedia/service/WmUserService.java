package com.swust.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swust.model.common.dtos.ResponseResult;
import com.swust.model.wemedia.dtos.WmLoginDto;
import com.swust.model.wemedia.pojos.WmUser;

public interface WmUserService extends IService<WmUser> {

    /**
     * 自媒体端登录
     * @param dto
     * @return
     */
    ResponseResult login(WmLoginDto dto);

}