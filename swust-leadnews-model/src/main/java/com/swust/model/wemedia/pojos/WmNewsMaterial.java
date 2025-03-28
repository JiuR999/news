package com.swust.model.wemedia.pojos;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 自媒体图文引用素材信息表
 * </p>
 *
 * @author Zhangxu
 * @since 2024-08-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("wm_news_material")
@ApiModel(value="WmNewsMaterial对象", description="自媒体图文引用素材信息表")
public class WmNewsMaterial implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "素材ID")
    private Integer materialId;

    @ApiModelProperty(value = "图文ID")
    private Integer newsId;

    @ApiModelProperty(value = "引用类型 0 内容引用 1 主图引用")
    private Integer type;

    @ApiModelProperty(value = "引用排序")
    private Integer ord;


}
