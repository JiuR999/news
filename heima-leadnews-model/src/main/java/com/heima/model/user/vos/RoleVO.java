package com.heima.model.user.vos;

import com.heima.model.user.pojos.ApUser;
import com.heima.model.user.pojos.SwustSystemMenu;
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
