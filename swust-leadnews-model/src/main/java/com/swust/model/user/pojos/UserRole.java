package com.swust.model.user.pojos;

import lombok.Data;

import java.util.List;

@Data
public class UserRole {
    private Integer id;
    private Integer key;
//    private Integer roleId;
    private String label;
    private Integer parentId;
    private String title;
    private String icon;
    private List<UserRole> children;
}
