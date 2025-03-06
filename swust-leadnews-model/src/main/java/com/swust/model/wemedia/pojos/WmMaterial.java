package com.swust.model.wemedia.pojos;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 自媒体图文素材信息表
 * </p>
 *
 * @author Zhangxu
 * @since 2024-08-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("wm_file")
@ApiModel(value="WmMaterial对象", description="自媒体图文素材信息表")
public class WmMaterial implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "自媒体用户ID")
    private Integer userId;

    @ApiModelProperty(value = "文件地址")
    private String url;

    @ApiModelProperty(value = "文件名称")
    private String fileName;

    @ApiModelProperty(value = "素材类型 0 图片 1 视频")
    private Short type;

    @ApiModelProperty(value = "是否收藏")
    private Short isCollection;

    @ApiModelProperty(value = "是否公开")
    private Short isPublic;

    @ApiModelProperty(value = "创建时间")
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;


}
