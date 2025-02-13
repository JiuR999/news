package com.heima.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.user.pojos.SwustSystemMenu;
import com.heima.model.user.vos.MenuVO;

import java.util.List;

public interface SystemMenuService extends IService<SwustSystemMenu> {
    List<MenuVO> listMenu();
}
