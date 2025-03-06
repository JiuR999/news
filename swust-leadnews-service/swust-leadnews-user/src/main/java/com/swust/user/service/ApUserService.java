package com.swust.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swust.model.common.dtos.ResponseResult;
import com.swust.model.user.dtos.LoginDto;
import com.swust.model.user.dtos.UserDto;
import com.swust.model.user.pojos.ApUser;
import com.swust.model.user.vos.UserVO;

import java.util.List;

public interface ApUserService extends IService<ApUser> {
    /**
     * app端登录功能
     * @param dto
     * @return
     */
    ResponseResult login(LoginDto dto);

    List<UserVO> page(UserDto userDto);

    UserVO getById(Long id);

    void resetPassword();

    Boolean add(ApUser userDto);

    UserVO currentUser(String token);

    ResponseResult logout(String token);

    ResponseResult auth(String token);
}
