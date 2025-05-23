package com.swust.model.wemedia.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
public class WmNewsDto {

    private Integer id;
    private Integer userId;
    /**
     * 标题
     */
    private String title;
    /**
     * 频道id
     */
    private Integer channelId;
    /**
     * 标签
     */
    private String labels;
    /**
     * 发布时间
     */
    private LocalDateTime publishTime;
    /**
     * 文章内容
     */
    private String content;
    /**
     * 文章封面类型  0 无图 1 单图 3 多图 -1 自动
     */
    private Short type;
    /**
     * 提交时间
     */
    private LocalDateTime submitedTime;
    /**
     * 状态 提交为1  草稿为0
     */
    private Short status;
    /**
     * 审核不通过理由
     */
    private String reason;
    /**
     * 封面图片列表 多张图以逗号隔开
     */
    private List<String> images;
}