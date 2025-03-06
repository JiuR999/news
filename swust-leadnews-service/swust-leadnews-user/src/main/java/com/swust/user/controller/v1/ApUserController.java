package com.swust.user.controller.v1;

import com.swust.model.common.dtos.ResponseResult;
import com.swust.model.user.dtos.LoginDto;
import com.swust.model.user.dtos.UserDto;
import com.swust.model.user.pojos.ApUser;
import com.swust.user.service.ApUserService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

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

    @PostMapping("/logout")
    public ResponseResult logout(String token){
        return userService.logout(token);
    }

    @PostMapping("/auth")
    public ResponseResult auth(String token){
        return userService.auth(token);
    }
    @GetMapping("/currentUser")
    public ResponseResult currentUser(HttpServletRequest request) {
        String token = request.getHeader("Token");
        return ResponseResult.okResult(userService.currentUser(token));
    }

    @PostMapping("/page")
    public ResponseResult page(@RequestBody(required = false) UserDto userDto) {
        return ResponseResult.okResult(userService.page(userDto));
    }
    @GetMapping("/getById/{id}")
    public ResponseResult info(@PathVariable Long id) {
        return ResponseResult.okResult(userService.getById(id));
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
