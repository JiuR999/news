package com.swust.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swust.model.common.dtos.PageResponseResult;
import com.swust.model.common.dtos.ResponseResult;
import com.swust.model.common.enums.AppHttpCodeEnum;
import com.swust.model.wemedia.dtos.WmMateriaDto;
import com.swust.model.wemedia.dtos.WmQueryDto;
import com.swust.model.wemedia.pojos.WmMaterial;
import com.swust.model.wemedia.pojos.WmUser;
import com.swust.model.wemedia.vos.WmMaterialVO;
import com.swust.service.FileStorageService;
import com.swust.utils.common.WmThreadLocalUtil;
import com.swust.wemedia.mapper.WmMaterialMapper;
import com.swust.wemedia.service.IWmMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

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
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

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
    public ResponseResult upload(MultipartFile[] multipartFiles) {
        //1.检查参数
        if (multipartFiles == null || multipartFiles.length == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        List<CompletableFuture<String>> futures = Arrays.stream(multipartFiles).map(file -> CompletableFuture.supplyAsync(() -> {
            // 原有单个文件上传逻辑
            //2.上传图片到minIO中
            String fileName = UUID.randomUUID().toString().replace("-", "");
            //test.jpg
            String originalFilename = file.getOriginalFilename();
            String postfix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileId = null;
            try {
                fileId = fileStorageService.uploadCommonFile("", fileName + postfix, file.getContentType(), file.getInputStream());
                log.info("上传文件到MinIO中，fileId:{}", fileId);
            } catch (IOException e) {
                e.printStackTrace();
                log.error("WmMaterialServiceImpl-上传文件失败");
            }
            return fileId;

        })).toList();
        List<String> urls = futures.stream().map(CompletableFuture::join).toList();
        return ResponseResult.okResult(urls);
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

    public ResponseResult list(WmQueryDto wmQueryDto) {
        wmQueryDto.checkParam();
/*        IPage page = new Page(wmQueryDto.getPage(), wmQueryDto.getSize());
        LambdaQueryWrapper<WmMaterial> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (wmQueryDto.getIsCollection() != null && wmQueryDto.getIsCollection() == 1) {
            lambdaQueryWrapper.eq(WmMaterial::getIsCollection, wmQueryDto.getIsCollection());
        }
        lambdaQueryWrapper.eq(wmQueryDto.getUserId() != null, WmMaterial::getUserId, wmQueryDto.getUserId());
        lambdaQueryWrapper.orderByDesc(WmMaterial::getCreatedTime);
        page = page(page, lambdaQueryWrapper);*/
        List<WmMaterialVO> vos = this.baseMapper.list(wmQueryDto, (wmQueryDto.getPage() - 1) * wmQueryDto.getSize(), wmQueryDto.getSize());
        PageResponseResult result = new PageResponseResult(wmQueryDto.getPage(), wmQueryDto.getSize(), vos.size());
        result.setData(vos);
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

    @Override
    public ResponseResult add(WmMateriaDto dto) {
        ArrayList<WmMaterial> materials = new ArrayList<>();
        if (dto.getFiles() == null || dto.getFiles().size() == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //校验是否登录
        WmUser user = WmThreadLocalUtil.getUser();
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        for (String fileName : dto.getFiles().keySet()) {
            WmMaterial material = new WmMaterial();
            material.setCreatedTime(LocalDateTime.now());
            material.setIsCollection((short) 0);
            material.setFileName(fileName);
            material.setIsPublic(dto.getIsPublic());
            material.setUrl(dto.getFiles().get(fileName));
            material.setUserId(user.getId());
            materials.add(material);
        }

        return ResponseResult.okResult(this.saveBatch(materials));
    }
}