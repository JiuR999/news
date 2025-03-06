package com.swust.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swust.model.user.pojos.SwustSystemMenu;
import com.swust.model.user.vos.MenuVO;

import java.util.List;

public interface SystemMenuService extends IService<SwustSystemMenu> {
    List<MenuVO> listMenu();
}
