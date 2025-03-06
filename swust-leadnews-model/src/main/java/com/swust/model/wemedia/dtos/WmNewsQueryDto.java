package com.swust.model.wemedia.dtos;

import com.swust.model.common.dtos.PageRequestDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper=false)
public class WmNewsQueryDto extends PageRequestDto {
    private LocalDateTime beginDate;

    private LocalDateTime endDate;

    private String keyword;

    @ApiModelProperty(value = "当前状态 0 草稿 1 提交（待审核） 2 审核失败 3 人工审核 4 人工审核通过 8 审核通过（待发布） 9 已发布")
    private Integer status;

    @ApiModelProperty(value = "上传人Id")
    private String userId;

    @ApiModelProperty(value = "上传人")
    private String author;

    @ApiModelProperty(value = "图文频道ID")
    private Integer channelId;
}
