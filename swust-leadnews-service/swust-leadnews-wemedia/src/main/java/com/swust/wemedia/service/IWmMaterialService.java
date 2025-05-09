package com.swust.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.swust.model.common.dtos.ResponseResult;
import com.swust.model.wemedia.dtos.WmAuditDto;
import com.swust.model.wemedia.dtos.WmMateriaDto;
import com.swust.model.wemedia.dtos.WmQueryDto;
import com.swust.model.wemedia.pojos.WmMaterial;
import com.swust.model.wemedia.vos.WmMaterialVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>
 * 自媒体图文素材信息表 服务类
 * </p>
 *
 * @author Zhangxu
 * @since 2024-08-25
 */
public interface IWmMaterialService extends IService<WmMaterial> {

    ResponseResult uploadPicture(MultipartFile multipartFile);
    ResponseResult upload(MultipartFile[] multipartFiles);
    void download(String url, HttpServletResponse response);
    ResponseResult list(WmQueryDto wmQueryDto);

    ResponseResult delete(Integer id);

    ResponseResult collect(Integer id);

    ResponseResult add(WmMateriaDto dto) throws IOException;

    ResponseResult audit(WmAuditDto dto);

    ResponseResult listFileType();

    ResponseResult sync();
}
