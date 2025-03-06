package com.swust.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.swust.model.user.dtos.UserRoleDto;
import com.swust.model.user.pojos.SwustRole;
import com.swust.model.user.pojos.UserRole;
import com.swust.model.user.vos.RoleVO;
import com.swust.user.mapper.RoleMapper;
import com.swust.user.mapper.UserRoleMapper;
import com.swust.user.service.UserRoleService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.List;

@Service
public class UserRoleServiceImpl implements UserRoleService {
    @Autowired
    UserRoleMapper userRoleMapper;
    @Autowired
    RoleMapper roleMapper;

    @Override
    public List<RoleVO> list() {
        List<RoleVO> vos = userRoleMapper.list();
        for (int i = 0; i < vos.size(); i++) {
            vos.get(i).setMenus(userRoleMapper.selectMenuIdsByRoleId(Integer.valueOf(vos.get(i).getId())));
        }
        return vos;
    }

    @Override
    public List<UserRole> getById(Integer roleId) {
        List<UserRole> menus = userRoleMapper.getByRoleId(roleId);
        List<UserRole> res = new ArrayList<>();
        HashMap<Integer, UserRole> tmpParentMap = new HashMap<>();
//        HashMap<Integer, List<MenuVO>> map = new HashMap<>();
        for (UserRole userRole : menus) {
            userRole.setChildren(new ArrayList<>());
//            MenuVO menuVO = new MenuVO();
//            menuVO.setChildren(new ArrayList<>());
//            menuVO.setIcon(userRole.getIcon());
//            menuVO.setKey(userRole.getKey());
//            menuVO.setLabel(userRole.getLabel());
//            menuVO.setTitle(userRole.getLabel());
            if (userRole.getParentId() == 0) {
                tmpParentMap.put(userRole.getKey(), userRole);
            }
            for (int i = 0; i < menus.size(); i++) {
                UserRole tmp = menus.get(i);
                if (Objects.equals(tmp.getParentId(), userRole.getKey())) {
//                    MenuVO menuVO1 = new MenuVO();
//                    menuVO1.setChildren(new ArrayList<>());
//                    menuVO1.setIcon(tmp.getIcon());
//                    menuVO1.setKey(tmp.getKey());
//                    menuVO1.setLabel(tmp.getLabel());
//                    menuVO1.setTitle(tmp.getLabel());
//                    UserRole vo = tmpParentMap.get(userRole.getId());
                    userRole.getChildren().add(tmp);
                }
            }
        }
        for (Map.Entry<Integer, UserRole> entry : tmpParentMap.entrySet()) {
            res.add(entry.getValue());
        }
        return res;
    }

    @Transactional
    @Override
    public Boolean update(UserRoleDto userRoleDto) {
        if (userRoleDto.getRoleId() == null || userRoleDto.getRoles().length <= 0) {
            return false;
        }
        //修改角色名称
        if (StringUtils.isNotEmpty(userRoleDto.getRoleName())) {
            roleMapper.update(new UpdateWrapper<SwustRole>()
                    .set("name", userRoleDto.getRoleName())
                    .eq("id", userRoleDto.getRoleId()));
        }

        //删除旧权限
        Boolean deleted = userRoleMapper.delete(userRoleDto.getRoleId());
        //删除用户-角色映射表中用户数据
        userRoleMapper.deleteUsers(userRoleDto.getRoleId());
        //增加用户
        //向角色-用户表增加映射
        if (userRoleDto.getUserIds() != null) {
            for (Integer userId : userRoleDto.getUserIds()) {
                userRoleMapper.add(userRoleDto.getRoleId(), userId);
            }
        }

        Boolean batched = userRoleMapper.batchInsert(userRoleDto.getRoleId(), userRoleDto.getRoles());
        return deleted && batched;
    }

    @Override
    @Transactional
    public Boolean add(UserRoleDto userRoleDto) {
        //增加角色
        SwustRole role = new SwustRole();
        role.setName(userRoleDto.getRoleName());
        //插入权限
        roleMapper.insert(role);

        //向角色-用户表增加映射
        if (userRoleDto.getUserIds() != null) {
            for (Integer userId : userRoleDto.getUserIds()) {
                userRoleMapper.add(role.getId(), userId);
            }
        }
        //向角色-权限表增加映射
        if (userRoleDto.getRoles() != null) {
            userRoleMapper.batchInsert(role.getId(), userRoleDto.getRoles());
        }
        return true;
    }
}
