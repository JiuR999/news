package com.swust.user.service;

import com.swust.model.user.dtos.UserRoleDto;
import com.swust.model.user.pojos.UserRole;
import com.swust.model.user.vos.RoleVO;

import java.util.List;

public interface UserRoleService {

    List<RoleVO> list();

    List<UserRole> getById(Integer roleId);
    Boolean update(UserRoleDto userRoleDto);
    Boolean add(UserRoleDto userRoleDto);
}
