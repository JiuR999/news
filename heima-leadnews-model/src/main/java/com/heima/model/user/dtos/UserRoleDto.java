package com.heima.model.user.dtos;

import lombok.Data;

@Data
public class UserRoleDto {
    //角色ID
    private Integer roleId;
    //角色得权限id列表
    private Integer[] roles;
    //角色名称
    private String roleName;
    private Integer[] userIds;
}
