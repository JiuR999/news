package com.swust.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swust.common.constants.AuditConstants;
import com.swust.model.common.dtos.PageResponseResult;
import com.swust.model.common.dtos.ResponseResult;
import com.swust.model.common.enums.AppHttpCodeEnum;
import com.swust.model.common.enums.TaskTypeEnum;
import com.swust.model.wemedia.dtos.WmAuditDto;
import com.swust.model.wemedia.dtos.WmMateriaDto;
import com.swust.model.wemedia.dtos.WmMaterialSearchDto;
import com.swust.model.wemedia.dtos.WmQueryDto;
import com.swust.model.wemedia.pojos.WmMaterial;
import com.swust.model.wemedia.pojos.WmUser;
import com.swust.model.wemedia.vos.WmMaterialVO;
import com.swust.service.FileStorageService;
import com.swust.utils.common.WmThreadLocalUtil;
import com.swust.wemedia.mapper.WmMaterialMapper;
import com.swust.wemedia.service.IWmMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
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
    private static final String TIKA_SERVER_URL = "http://127.0.0.1:9998/tika"; // 默认Tika服务地址
    final FileStorageService fileStorageService;
    final WmNewsService wmNewsService;
    RestClient client;
    public WmMaterialServiceImpl(FileStorageService fileStorageService,
                                 WmNewsService wmNewsService,
                                 RestClient restClient) {
        this.fileStorageService = fileStorageService;
        this.wmNewsService = wmNewsService;
        this.client = restClient;
    }

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
            assert originalFilename != null;
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
            String url = getById(id).getUrl();
            boolean removed = removeById(id);
            //从MinIO删除
            if (removed) {
                fileStorageService.delete(url);
            }
            return !removed ? ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST)
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
        if (dto.getFiles() == null || dto.getFiles().isEmpty()) {
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
            String url = dto.getFiles().get(fileName);
            material.setUrl(url);
            //将文件解析内容然后存ES
            try {
                String content = parseDocumentFromURL(url);
                material.setUserId(user.getId());
                material.setStatus(AuditConstants.SHOULD_AUDIT);
                material.setChannelId(dto.getChannelId());
                WmMaterialSearchDto searchDto = new WmMaterialSearchDto();
                BeanUtils.copyProperties(material, searchDto);
                searchDto.setFileContent(content);
                // TODO: 2023/4/17 搜索内容存到ES
                boolean saved = this.save(material);
                if(saved) {
                    //提交到审核队列
                    if (saved) {
                        //加入队列 随机时间执行
                        wmNewsService.addNewsToTask(searchDto, TaskTypeEnum.FILE_AUDIT_TIME, LocalDateTime.now().plusSeconds(25));
                    }
                } else {
                    return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
                }
                materials.add(material);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return ResponseResult.okResult(true);
    }

    @Override
    public ResponseResult audit(WmAuditDto dto) {
        WmMaterial material = new WmMaterial();
        material.setId(Math.toIntExact(dto.getId()));
        material.setStatus(Objects.equals(dto.getStatus(), AuditConstants.SHOULD_AUDIT) ? AuditConstants.PUBLISHED : AuditConstants.FAILED_TO_AUDIT);
        if (!StringUtils.isBlank(dto.getReason())) {
            material.setReason(dto.getReason());
        }
        int updated = this.baseMapper.updateById(material);
        if(updated < 1) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        material = this.getById(material.getId());
        WmMaterialSearchDto searchDto = new WmMaterialSearchDto();
        BeanUtils.copyProperties(material, searchDto);
        searchDto.setFileContent(dto.getFileContent());
        //写入Es
        GetIndexRequest request = new GetIndexRequest("knowledge");
        return ResponseResult.okResult(updated);
    }

    @Override
    public ResponseResult listFileType() {
        List<String> fileType = this.baseMapper.listFileType();
        return ResponseResult.okResult(fileType);
    }

    public static String parseDocumentFromURL(String fileUrl) throws IOException {
        // 从URL下载文件内容
        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        InputStream inputStream = connection.getInputStream();

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        byte[] downloadedFileBytes = buffer.toByteArray();

        // 向Tika服务器发送请求并获取响应
        URL tikaUrl = new URL(TIKA_SERVER_URL);
        HttpURLConnection tikaConnection = (HttpURLConnection) tikaUrl.openConnection();
        tikaConnection.setDoOutput(true);
        tikaConnection.setRequestMethod("PUT"); // 使用PUT方法上传文件
        tikaConnection.setRequestProperty("Content-Type", "application/octet-stream");
        tikaConnection.setRequestProperty("Accept", "text/plain");
        try (OutputStream os = tikaConnection.getOutputStream()) {
            os.write(downloadedFileBytes);
        }
        //获取文件内容解析内容
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(tikaConnection.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        } finally {
            tikaConnection.disconnect();
        }

        return response.toString();
    }
}