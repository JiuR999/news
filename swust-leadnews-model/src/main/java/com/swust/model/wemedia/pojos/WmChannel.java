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
 * 频道信息表
 * </p>
 *
 * @author Zhangxu
 * @since 2024-08-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("wm_channel")
@ApiModel(value="WmChannel对象", description="频道信息表")
public class WmChannel implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "频道名称")
    private String name;

    @ApiModelProperty(value = "频道描述")
    private String description;

    @ApiModelProperty(value = "是否默认频道")
    private Integer isDefault;

    private Integer status;

    @ApiModelProperty(value = "默认排序")
    private Integer ord;

    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;


}
