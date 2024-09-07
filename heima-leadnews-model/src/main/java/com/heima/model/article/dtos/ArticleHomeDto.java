package com.heima.model.article.dtos;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class ArticleHomeDto {

    // 最大时间
    @ApiModelProperty("最大时间")
    Date maxBehotTime;
    // 最小时间
    @ApiModelProperty("最小时间")
    Date minBehotTime;
    // 分页size
    @ApiModelProperty("分页时间")
    Integer size;
    // 频道ID
    @ApiModelProperty("标签")
    String tag;
}