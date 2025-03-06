package com.swust.wemedia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swust.model.wemedia.dtos.WmNewsQueryDto;
import com.swust.model.wemedia.pojos.WmNews;
import com.swust.model.wemedia.vos.WmNewVO;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WmNewsMapper extends BaseMapper<WmNews> {
    WmNewVO selectWithAuthorById(Long id);
    List<WmNewVO> list(@Param("dto") WmNewsQueryDto dto,@Param("offset") Integer offset,@Param("limit") Integer limit);
    Integer count(WmNewsQueryDto dto);
}
