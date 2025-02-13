package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.service.FileStorageService;
import com.heima.utils.common.WmThreadLocalUtil;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.service.IWmMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.html.Option;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * <p>
 * 自媒体图文素材信息表 服务实现类
 * </p>
 *
 * @author Zhangxu
 * @since 2024-08-25
 */
@Service
@Slf4j
public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper, WmMaterial> implements IWmMaterialService {

    @Autowired
    FileStorageService fileStorageService;

    @Override
    public ResponseResult uploadPicture(MultipartFile multipartFile) {
        //1.检查参数
        if (multipartFile == null || multipartFile.getSize() == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //2.上传图片到minIO中
        String fileName = UUID.randomUUID().toString().replace("-", "");
        //test.jpg
        String originalFilename = multipartFile.getOriginalFilename();

        String postfix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileId = null;
        try {
            fileId = fileStorageService.uploadImgFile("", fileName + postfix, multipartFile.getContentType(), multipartFile.getInputStream());
            log.info("上传图片到MinIO中，fileId:{}", fileId);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("WmMaterialServiceImpl-上传文件失败");
        }
        WmMaterial wmMaterial = new WmMaterial();
        wmMaterial.setUrl(fileId);
        wmMaterial.setUserId(WmThreadLocalUtil.getUser().getId());
        wmMaterial.setIsCollection((short) 0);
        wmMaterial.setType((short) 0);
        wmMaterial.setCreatedTime(LocalDateTime.now());
        save(wmMaterial);
        return ResponseResult.okResult(wmMaterial);
    }

    @Override
    public ResponseResult uploadPicture2(MultipartFile multipartFile) {
        //1.检查参数
        if (multipartFile == null || multipartFile.getSize() == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //2.上传图片到minIO中
        String fileName = UUID.randomUUID().toString().replace("-", "");
        //test.jpg
        String originalFilename = multipartFile.getOriginalFilename();

        String postfix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileId = null;
        try {
            fileId = fileStorageService.uploadCommonFile("", fileName + postfix, multipartFile.getContentType(), multipartFile.getInputStream());
            log.info("上传文件到MinIO中，fileId:{}", fileId);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("WmMaterialServiceImpl-上传文件失败");
        }

        return ResponseResult.okResult(fileId);
    }

    @Override
    public void download(String url, HttpServletResponse response) {
        String fileName = url.substring(url.lastIndexOf('/'));
        byte[] bytes = fileStorageService.downLoadFile(url);
        try {
            response.setContentType("application/octet-stream");
//            response.setContentType(MediaType.IMAGE_JPEG_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "inline; filename=" + fileName);
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(bytes);
        } catch (IOException e) {
            log.error("下载文件:" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public ResponseResult<WmMaterial> list(WmMaterialDto wmMaterialDto) {
        wmMaterialDto.checkParam();
        IPage page = new Page(wmMaterialDto.getPage(), wmMaterialDto.getSize());
        LambdaQueryWrapper<WmMaterial> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (wmMaterialDto.getIsCollection() != null && wmMaterialDto.getIsCollection() == 1) {
            lambdaQueryWrapper.eq(WmMaterial::getIsCollection, wmMaterialDto.getIsCollection());
        }
        lambdaQueryWrapper.eq(WmMaterial::getUserId, WmThreadLocalUtil.getUser().getId());
        lambdaQueryWrapper.orderByDesc(WmMaterial::getCreatedTime);
        page = page(page, lambdaQueryWrapper);
        PageResponseResult result = new PageResponseResult(wmMaterialDto.getPage(), wmMaterialDto.getSize(), (int) page.getTotal());
        result.setData(page.getRecords());
        return result;
    }

    @Override
    public ResponseResult delete(Integer id) {
        Optional<Integer> optional = Optional.ofNullable(id);
        if (optional.isPresent()) {
            boolean removed = removeById(id);
            return removed == false ? ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST)
                    : ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }
        return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
    }

    @Override
    public ResponseResult collect(Integer id) {
        UpdateWrapper<WmMaterial> wrapper = new UpdateWrapper<>();
        WmMaterial material = getById(id);
        wrapper.eq("id", id);
        wrapper.set("is_collection", material.getIsCollection() == 0 ? 1 : 0);
        update(null, wrapper);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}