package com.swust.wemedia.controller.v1;

import com.swust.model.common.dtos.ResponseResult;
import com.swust.model.wemedia.dtos.WmLoginDto;
import com.swust.model.wemedia.pojos.WmUser;
import com.swust.wemedia.service.WmUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    @Autowired
    private WmUserService wmUserService;

    @PostMapping("/login")
    public ResponseResult login(@RequestBody WmLoginDto dto){
        return ResponseResult.okResult(wmUserService.login(dto));
    }

    @PostMapping("/add")
    public ResponseResult add(@RequestBody WmUser dto){
        return ResponseResult.okResult(wmUserService.save(dto));
    }
}
