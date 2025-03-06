package com.swust.model.wemedia.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class WmMateriaDto {
    //频道id
    private Integer channelId;
    //是否公开
    private Short isPublic;
    //附件
    private Map<String,String> files;
    private LocalDateTime beginDate;

    private LocalDateTime endDate;
}
