package com.heima.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.LoginDto;
import com.heima.model.user.dtos.UserDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.user.vos.UserVO;

import java.util.List;

public interface ApUserService extends IService<ApUser> {
    /**
     * app端登录功能
     * @param dto
     * @return
     */
    ResponseResult login(LoginDto dto);

    List<UserVO> page(UserDto userDto);

    ApUser info();

    void resetPassword();

    Boolean add(ApUser userDto);

    UserVO currentUser(Integer token);
}
