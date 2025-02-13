package com.heima.user.service;

import com.heima.model.user.dtos.UserRoleDto;
import com.heima.model.user.pojos.UserRole;
import com.heima.model.user.vos.MenuVO;
import com.heima.model.user.vos.RoleVO;

import java.util.List;

public interface UserRoleService {

    List<RoleVO> list();

    List<UserRole> getById(Integer roleId);
    Boolean update(UserRoleDto userRoleDto);
    Boolean add(UserRoleDto userRoleDto);
}
