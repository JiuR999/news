package com.heima.model.user.pojos;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.beans.Transient;
import java.io.Serializable;
import java.util.List;

import com.github.xiaoymin.knife4j.annotations.Ignore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author Zhangxu
 * @since 2025-02-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("swust_system_menu")
@ApiModel(value="SwustSystemMenu对象", description="")
public class SwustSystemMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "权限名称")
    private String name;

    @ApiModelProperty(value = "权限关联资源")
    private String path;

    @ApiModelProperty(value = "父节点id")
    private Integer parentId;

    private String title;

    private String icon;

    @TableField(exist = false)
    private List<SwustSystemMenu> children;
}
