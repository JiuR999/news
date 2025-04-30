package com.swust.wemedia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swust.model.wemedia.dtos.WmQueryDto;
import com.swust.model.wemedia.pojos.WmMaterial;
import com.swust.model.wemedia.vos.WmMaterialVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 自媒体图文素材信息表 Mapper 接口
 * </p>
 *
 * @author Zhangxu
 * @since 2024-08-25
 */
public interface WmMaterialMapper extends BaseMapper<WmMaterial> {
    List<WmMaterialVO> list(@Param("dto") WmQueryDto dto, Integer offset, Integer size);

    List<String> listFileType();
}
