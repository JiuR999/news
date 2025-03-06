package com.swust.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swust.model.user.pojos.SwustRole;
import com.swust.user.mapper.RoleMapper;
import com.swust.user.service.RoleService;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, SwustRole> implements RoleService {
}
