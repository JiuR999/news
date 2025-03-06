package com.swust.wemedia.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swust.model.wemedia.pojos.WmUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WmUserMapper extends BaseMapper<WmUser> {
}