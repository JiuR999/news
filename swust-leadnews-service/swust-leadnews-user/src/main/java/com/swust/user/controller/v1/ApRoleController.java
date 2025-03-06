package com.swust.user.controller.v1;

import com.swust.model.common.dtos.ResponseResult;
import com.swust.user.service.RoleService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/role")
public class ApRoleController {

    @Autowired
    RoleService roleService;
    @GetMapping("/list")
    @ApiOperation("获取所有角色")
    public ResponseResult list() {
        return ResponseResult.okResult(roleService.list());
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除角色")
    public ResponseResult delete(@PathVariable Integer id) {
        return ResponseResult.okResult(roleService.removeById(id));
    }
}
