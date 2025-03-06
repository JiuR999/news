package com.swust.model.user.vos;

import com.swust.model.user.pojos.ApUser;
import lombok.Data;

import java.util.List;

@Data
public class RoleVO {
    private String id;
    //角色名称
    private String name;
    private List<Integer> menus;
    //该角色包含的用户列表
    private List<ApUser> users;
}
