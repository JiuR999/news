package com.swust.model.wemedia.pojos;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 自媒体图文内容信息表
 * </p>
 *
 * @author Zhangxu
 * @since 2024-08-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("wm_news")
@ApiModel(value="WmNews对象", description="自媒体图文内容信息表")
public class WmNews implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "自媒体用户ID")
    private Integer userId;

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "图文内容")
    private String content;

    @ApiModelProperty(value = "文章布局 0 无图文章 1 单图文章 3 多图文章")
    private Integer type;

    @ApiModelProperty(value = "图文频道ID")
    private Integer channelId;

    private String labels;

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "created_time",fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    @ApiModelProperty(value = "提交时间")
    private LocalDateTime submitedTime;

    @ApiModelProperty(value = "当前状态 0 草稿 1 提交（待审核） 2 审核失败 3 人工审核 4 人工审核通过 8 审核通过（待发布） 9 已发布 -1 发布失败")
    private Short status;

    @ApiModelProperty(value = "定时发布时间，不定时则为空")
    private LocalDateTime publishTime;

    @ApiModelProperty(value = "拒绝理由")
    private String reason;

    @ApiModelProperty(value = "发布库文章ID")
    private Long articleId;

    @ApiModelProperty(value = "//图片用逗号分隔")
    private String images;

    private Integer enable;


}
