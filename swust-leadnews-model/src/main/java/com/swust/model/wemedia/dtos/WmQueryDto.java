package com.swust.model.wemedia.dtos;

import com.swust.model.common.dtos.PageRequestDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
public class WmQueryDto extends PageRequestDto {
    /**
     * 1 收藏
     * 0 未收藏
     */
    private Short isCollection;
    /**
     * 1 公开
     * 0 私密
     */
    private Short isPublic;
    /**
     * 作者
     */
    private String author;

    private Integer userId;
    private Integer channelId;

    private LocalDateTime beginDate;
    private LocalDateTime endDate;
}
