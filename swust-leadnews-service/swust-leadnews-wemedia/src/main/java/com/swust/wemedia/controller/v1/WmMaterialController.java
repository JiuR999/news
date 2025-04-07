package com.swust.wemedia.controller.v1;

import com.swust.model.common.dtos.IdsDto;
import com.swust.model.common.dtos.ResponseResult;
import com.swust.model.wemedia.dtos.WmMateriaDto;
import com.swust.model.wemedia.dtos.WmAuditDto;
import com.swust.model.wemedia.dtos.WmQueryDto;
import com.swust.wemedia.service.IWmMaterialService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1/material")
@Api(value = "素材管理", tags = "素材管理接口")
public class WmMaterialController {
    @Autowired
    IWmMaterialService wmMaterialService;

    @PostMapping("/upload_picture")
    @ApiOperation(value = "上传图片素材")
    @ApiImplicitParam(name = "multipartFile", value = "图片文件", required = true, dataType = "MultipartFile")
    public ResponseResult uploadPicture(MultipartFile multipartFile){
        return wmMaterialService.uploadPicture(multipartFile);
    }

    @PostMapping("/upload")
    @ApiOperation(value = "批量上传素材")
    @ApiImplicitParam(name = "files", value = "文件数组", required = true, dataType = "MultipartFile[]")
    public ResponseResult upload(@RequestParam("files")  MultipartFile[] multipartFiles){
        return wmMaterialService.upload(multipartFiles);
    }

    @GetMapping("/download")
    @ApiOperation(value = "下载素材文件", produces = "application/octet-stream")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "url", value = "文件下载地址", required = true),
            @ApiImplicitParam(name = "response", value = "响应对象", dataType = "HttpServletResponse")
    })
    public void download(String url, HttpServletResponse response) {
        wmMaterialService.download(url,response);
    }

    /**
     * 分页查询素材列表
     *
     * @param wmQueryDto 素材查询参数对象，包含以下字段：
     *                      - page 当前页码
     *                      - size 每页显示数量
     *                      - isCollection 收藏状态筛选条件（可选）
     *                      - status 素材状态筛选条件（可选）
     * @return 统一响应结果对象，包含：
     *         - code 响应状态码
     *         - message 响应描述信息
     *         - data 分页数据结果集，包含：
     *           - total 总记录数
     *           - pages 总页数
     *           - records 当前页数据列表
     */
    @PostMapping("/list")
    @ApiOperation("分页查询素材列表")
    @ApiImplicitParam(name = "wmQueryDto", value = "查询参数", required = true, dataTypeClass = WmQueryDto.class)
    public ResponseResult List(@RequestBody WmQueryDto wmQueryDto){
        return wmMaterialService.list(wmQueryDto);
    }


    @PostMapping("/add")
    @ApiOperation("新增素材关联关系")
    @ApiImplicitParam(name = "dto", value = "素材关联参数", required = true, dataTypeClass = WmMateriaDto.class)
    public ResponseResult add(@RequestBody WmMateriaDto dto){
        return wmMaterialService.add(dto);
    }

    @GetMapping("/del/{id}")
    @ApiOperation("删除素材文件")
    @ApiImplicitParam(name = "id", value = "素材ID", required = true, paramType = "path")
    public ResponseResult delete(@PathVariable Integer id) {
        return wmMaterialService.delete(id);
    }

    @PostMapping("/deleteBatch")
    @ApiOperation("根据ids删除素材")
    public ResponseResult deleteBatch(@RequestBody IdsDto ids) {
        return ResponseResult.okResult(wmMaterialService.removeBatchByIds(ids.getIds()));
    }

    @GetMapping("/collect/{id}")
    @ApiOperation("收藏/取消收藏素材")
    @ApiImplicitParam(name = "id", value = "素材ID", required = true, paramType = "path")
    public ResponseResult collect(@PathVariable Integer id) {
        return wmMaterialService.collect(id);
    }

    @PostMapping("/audit")
    @ApiOperation("审核文件")
    public ResponseResult audit(@RequestBody WmAuditDto dto) {
        return wmMaterialService.audit(dto);
    }
}