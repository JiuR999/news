package com.heima.user.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.UserRoleDto;
import com.heima.user.service.UserRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/userRole")
@Api(value = "用户角色接口", tags = "用户角色接口")
public class ApUserRoleController {
    @Autowired
    UserRoleService userRoleService;
    @GetMapping("/list")
    @ApiOperation("获取所有角色的用户列表")
    public ResponseResult list() {
        return ResponseResult.okResult(userRoleService.list());
    }

    @GetMapping("/getById")
    @ApiOperation("获取当前角色的权限列表")
    public ResponseResult getById(Integer roleId) {
        return ResponseResult.okResult(userRoleService.getById(roleId));
    }

    @PostMapping("/update")
    @ApiOperation("修改当前角色的权限")
    public ResponseResult update(@RequestBody UserRoleDto userRoleDto) {
        return ResponseResult.okResult(userRoleService.update(userRoleDto));
    }

    @PostMapping("/add")
    @ApiOperation("增加角色的权限")
    public ResponseResult add(@RequestBody UserRoleDto userRoleDto) {
        return ResponseResult.okResult(userRoleService.add(userRoleDto));
    }

}
