package com.swust.model.article.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArticleHomeDto {

    // 最大时间
    @ApiModelProperty("最大时间")
    Date maxBehotTime;
    // 最小时间
    @ApiModelProperty("最小时间")
    Date minBehotTime;
    // 分页size
    @ApiModelProperty("分页大小")
    Integer size;
    @ApiModelProperty("当前页码")
    Integer page;
    // 频道ID
    @ApiModelProperty("标签")
    String tag;
    // 关键字
    @ApiModelProperty("关键词")
    String keyWord;
    public void ifAbsent() {
        setPage(1);
        setSize(20);
    }
}