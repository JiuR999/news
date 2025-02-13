package com.heima.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.user.pojos.SwustSystemMenu;
import com.heima.model.user.vos.MenuVO;
import com.heima.user.mapper.SwustSystemMenuMapper;
import com.heima.user.service.SystemMenuService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemMenuServiceImpl extends ServiceImpl<SwustSystemMenuMapper, SwustSystemMenu> implements SystemMenuService {

    public List<MenuVO> listMenu() {
        List<MenuVO> res = new ArrayList<>();
        HashMap<Integer, MenuVO> tmpParentMap = new HashMap<>();
        List<SwustSystemMenu> menus = list();
//        List<Integer> parentIndexs = new ArrayList<>();
        for (SwustSystemMenu menu : menus) {

            MenuVO menuVO = buildMenuVO(menu);
            if (menu.getParentId() == 0) {
                tmpParentMap.put(menu.getId(), menuVO);
            }
            for (int i = 0; i < menus.size(); i++) {
                SwustSystemMenu tmp = menus.get(i);
                if (tmp.getParentId() == menu.getId()) {
                    MenuVO menuVO1 = buildMenuVO(tmp);
                    MenuVO parentMenuVO = tmpParentMap.get(menu.getId());
                    if (parentMenuVO != null) {
                        parentMenuVO.getChildren().add(menuVO1);
                    }
                }
            }
        }
        for (Map.Entry<Integer, MenuVO> entry : tmpParentMap.entrySet()) {
            res.add(entry.getValue());
        }
        return res;
    }

    private MenuVO buildMenuVO(SwustSystemMenu tmp) {
        MenuVO menuVO = new MenuVO();
        menuVO.setKey(tmp.getId());
        menuVO.setIcon(tmp.getIcon());
        menuVO.setLabel(tmp.getName());
        menuVO.setTitle(tmp.getName());
        menuVO.setPath(tmp.getPath());
        menuVO.setChildren(new ArrayList<>());
        return menuVO;
    }
}
