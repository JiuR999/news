package com.heima.wemedia.controller.v1;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.wemedia.service.IWmMaterialService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1/material")
@Api(value = "素材管理")
public class WmMaterialController {

    @Autowired
    IWmMaterialService wmMaterialService;

    @PostMapping("/upload_picture")
    public ResponseResult uploadPicture(MultipartFile multipartFile){
        return wmMaterialService.uploadPicture(multipartFile);
    }

    @PostMapping("/upload")
    public ResponseResult upload(MultipartFile multipartFile){
        return wmMaterialService.uploadPicture2(multipartFile);
    }

    @GetMapping("/download")
    public void download(String url, HttpServletResponse response) {
        wmMaterialService.download(url,response);
    }

    @PostMapping("/list")
    public ResponseResult List(@RequestBody WmMaterialDto wmMaterialDto){
        return wmMaterialService.list(wmMaterialDto);
    }

    @GetMapping("/del_picture/{id}")
    public ResponseResult delete(@PathVariable Integer id) {
        return wmMaterialService.delete(id);
    }

    @GetMapping("/collect/{id}")
    public ResponseResult collect(@PathVariable Integer id) {
        return wmMaterialService.collect(id);
    }
}