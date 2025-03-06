package com.swust.user.mapper;

import com.swust.model.user.pojos.UserRole;
import com.swust.model.user.vos.RoleVO;
import org.apache.ibatis.annotations.Mapper;


import java.util.List;

@Mapper
public interface UserRoleMapper {
    List<RoleVO> list();

    List<UserRole> getByRoleId(Integer roleId);
    RoleVO getByUserId(Integer userId);

    Boolean delete(Integer roleId);

    Boolean batchInsert(Integer roleId, Integer[] roles);
    Boolean add(Integer roleId, Integer userId);
    List<Integer> selectMenuIdsByRoleId(Integer id);

    List<String> selectMenuIdsByUserId(Integer userId);

    Boolean deleteUsers(Integer roleId);
}
