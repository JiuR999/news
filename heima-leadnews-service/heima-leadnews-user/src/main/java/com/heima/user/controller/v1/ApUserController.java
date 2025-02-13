package com.heima.user.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.LoginDto;
import com.heima.model.user.dtos.UserDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.wemedia.dtos.WmLoginDto;
import com.heima.user.service.ApUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@Api("用户管理接口")
public class ApUserController {

    @Autowired
    ApUserService userService;

    @PostMapping("/login")
    public ResponseResult login(@RequestBody LoginDto dto){
        return userService.login(dto);
    }

    @GetMapping("/currentUser")
    public ResponseResult currentUser(Integer id) {
        return ResponseResult.okResult(userService.currentUser(id));
    }

    @PostMapping("/page")
    public ResponseResult page(@RequestBody(required = false) UserDto userDto) {
        return ResponseResult.okResult(userService.page(userDto));
    }
    @GetMapping("/info")
    public ResponseResult info() {
        return ResponseResult.okResult(userService.info());
    }
    @PostMapping("/resetPassword")
    public ResponseResult resetPassword() {
        userService.resetPassword();
        return ResponseResult.okResult("操作成功！");
    }

    @PostMapping("/add")
    public ResponseResult add(@RequestBody ApUser userDto) {
        return ResponseResult.okResult(userService.add(userDto));
    }
    @PostMapping("/update")
    public ResponseResult update(@RequestBody ApUser userDto) {
        return ResponseResult.okResult(userService.updateById(userDto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseResult update(@PathVariable Integer id) {
        return ResponseResult.okResult(userService.removeById(id));
    }
}
