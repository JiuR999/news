package com.swust.model.wemedia.dtos;

import com.swust.model.common.dtos.PageRequestDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
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
    /**
     * 文件名
     */
    private String fileName;
    private Integer channelId;

    private LocalDate beginDate;
    private LocalDate endDate;
    /**
     * 当前状态
     */
    private Short status;

    /**
     * 文件类型
     */
    private String fileType;
}
