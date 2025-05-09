package com.swust.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.stream.Collectors;

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
    RestHighLevelClient restHighLevelClient;

    public WmMaterialServiceImpl(FileStorageService fileStorageService,
                                 WmNewsService wmNewsService,
                                 RestHighLevelClient restClient) {
        this.fileStorageService = fileStorageService;
        this.wmNewsService = wmNewsService;
        this.restHighLevelClient = restClient;
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

    public ResponseResult list2(WmQueryDto wmQueryDto) {
        wmQueryDto.checkParam();
        wmQueryDto.checkParam();

        // 构建 Elasticsearch 查询请求
        SearchRequest searchRequest = new SearchRequest("knowledge"); // 替换为你的索引名称
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        // 构建查询条件
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 添加用户ID过滤条件
        if (wmQueryDto.getKeyWord() != null) {
            boolQuery.must(QueryBuilders.termQuery("fileContent", wmQueryDto.getKeyWord()));
        }

        // 添加文件类型过滤条件                                              org.springframework.beans.factory.annotation.Autowired;
        if (wmQueryDto.getFileType() != null) {
            boolQuery.must(QueryBuilders.termQuery("fileName", wmQueryDto.getFileType()));
        }

        // 添加用户ID过滤条件
        if (wmQueryDto.getUserId() != null) {
            boolQuery.must(QueryBuilders.termQuery("userId", wmQueryDto.getUserId()));
        }

        // 添加收藏状态过滤条件
        if (wmQueryDto.getIsCollection() != null && wmQueryDto.getIsCollection() == 1) {
            boolQuery.must(QueryBuilders.termQuery("isCollection", 1));
        }

        // 设置分页
        sourceBuilder.from((wmQueryDto.getPage() - 1) * wmQueryDto.getSize());
        sourceBuilder.size(wmQueryDto.getSize());

        // 设置排序
        sourceBuilder.sort("createdTime", SortOrder.DESC);

        // 将查询条件添加到请求中
        sourceBuilder.query(boolQuery);
        searchRequest.source(sourceBuilder);

        // 执行查询
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse.getHits();

            // 将查询结果转换为 WmMaterialVO 列表
            List<WmMaterialVO> vos = Arrays.stream(hits.getHits())
                    .map(hit -> {
                        WmMaterialVO vo = JSON.parseObject(hit.getSourceAsString(), WmMaterialVO.class);
                        return vo;
                    })
                    .collect(Collectors.toList());

            // 构建分页响应结果
            PageResponseResult result = new PageResponseResult(wmQueryDto.getPage(), wmQueryDto.getSize(), (int) hits.getTotalHits().value);
            result.setData(vos);
            return result;
        } catch (IOException e) {
            log.error("Elasticsearch 查询失败", e);
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
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

        return list2(wmQueryDto);

/*        List<WmMaterialVO> vos = this.baseMapper.list(wmQueryDto, (wmQueryDto.getPage() - 1) * wmQueryDto.getSize(), wmQueryDto.getSize());
        PageResponseResult result = new PageResponseResult(wmQueryDto.getPage(), wmQueryDto.getSize(), vos.size());
        result.setData(vos);
        return result;*/
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
                material.setUserId(user.getId());
                material.setStatus(AuditConstants.SHOULD_AUDIT);
                material.setChannelId(dto.getChannelId());
                String content = parseDocumentFromURL(url);
                WmMaterialSearchDto searchDto = new WmMaterialSearchDto();
                // TODO: 2023/4/17 搜索内容存到ES
                boolean saved = this.save(material);
                if (saved) {
                    BeanUtils.copyProperties(material, searchDto);
                    searchDto.setFileContent(content);
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
        if (updated < 1) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        material = this.getById(material.getId());
        WmMaterialSearchDto searchDto = new WmMaterialSearchDto();
        BeanUtils.copyProperties(material, searchDto);
        searchDto.setFileContent(dto.getFileContent());
        //写入Es
        // 创建索引请求
        IndexRequest request = new IndexRequest("knowledge");
        request.id(searchDto.getId().toString()); // 设置文档ID
        request.source(JSON.toJSONString(searchDto), XContentType.JSON);
        IndexResponse indexResponse = null;
        try {
            indexResponse = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (indexResponse.status() == RestStatus.CREATED) {
            System.out.println(searchDto.getFileName() + "-----------" + "成功写入ES");
        } else {
            System.err.println(searchDto.getFileName() + "-----------" + "写入ES失败");
        }
        return ResponseResult.okResult(updated);
    }

    @Override
    public ResponseResult listFileType() {
        List<String> fileType = this.baseMapper.listFileType();
        return ResponseResult.okResult(fileType);
    }

    @Override
    public ResponseResult sync() {
        List<WmMaterial> materials = this.list(new LambdaQueryWrapper<WmMaterial>().eq(WmMaterial::getStatus, 9));
materials.stream().forEach(material -> {
    String content = null;
    try {
        content = parseDocumentFromURL(material.getUrl());
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
    WmMaterialSearchDto searchDto = new WmMaterialSearchDto();
    BeanUtils.copyProperties(material, searchDto);
    searchDto.setFileContent(content);

    IndexRequest request = new IndexRequest("knowledge");
    request.id(searchDto.getId().toString()); // 设置文档ID
    request.source(JSON.toJSONString(searchDto), XContentType.JSON);
    IndexResponse indexResponse = null;
    try {
        indexResponse = restHighLevelClient.index(request, RequestOptions.DEFAULT);
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
    if (indexResponse.status() == RestStatus.CREATED) {
        System.out.println(searchDto.getFileName() + "-----------" + "成功写入ES");
    } else {
        System.err.println(searchDto.getFileName() + "-----------" + "写入ES失败");
    }
});
        return ResponseResult.okResult(materials);
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