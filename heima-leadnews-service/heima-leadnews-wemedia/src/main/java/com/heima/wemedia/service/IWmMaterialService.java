package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import org.springframework.web.multipart.MultipartFile;

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
    ResponseResult<WmMaterial> list(WmMaterialDto wmMaterialDto);

    ResponseResult delete(Integer id);

    ResponseResult collect(Integer id);
}
