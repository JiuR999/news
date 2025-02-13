package com.heima.user.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.user.mapper.SwustSystemMenuMapper;
import com.heima.user.service.SystemMenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/menu")
@RestController
@Api(value = "权限列表接口", tags = "权限列表接口")
public class ApSystemMenuController {
    @Autowired
    SystemMenuService systemMenuService;
    @GetMapping("/list")
    @ApiOperation("获取所有权限列表")
    public ResponseResult list() {
        return ResponseResult.okResult(systemMenuService.listMenu());
    }
}
