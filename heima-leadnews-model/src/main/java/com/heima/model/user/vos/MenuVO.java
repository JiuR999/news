package com.heima.model.user.vos;

import com.heima.model.user.pojos.SwustSystemMenu;
import lombok.Data;

import java.util.List;

@Data
public class MenuVO {
    private Integer key;
    private String label;
    private String icon;
    private String path;
    private String title;
    private List<MenuVO> children;
}
