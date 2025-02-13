package com.heima.user.mapper;

import com.heima.model.user.pojos.UserRole;
import com.heima.model.user.vos.RoleVO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface UserRoleMapper {
    List<RoleVO> list();

    List<UserRole> getById(Integer roleId);

    Boolean delete(Integer roleId);

    Boolean batchInsert(Integer roleId, Integer[] roles);
    Boolean add(Integer roleId, Integer userId);
    List<Integer> selectMenuIdsByRoleId(Integer id);

    List<String> selectMenuIdsByUserId(Integer userId);

    Boolean deleteUsers(Integer roleId);
}
