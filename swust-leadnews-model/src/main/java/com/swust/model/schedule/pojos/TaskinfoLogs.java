package com.swust.model.schedule.pojos;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.sql.Blob;
import java.io.Serializable;
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
 * @since 2025-02-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("taskinfo_logs")
@ApiModel(value="TaskinfoLogs对象", description="")
public class TaskinfoLogs implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "任务id")
    @TableId(value = "task_id", type = IdType.AUTO)
    private Long taskId;

    @ApiModelProperty(value = "执行时间")
    private LocalDateTime executeTime;

    @ApiModelProperty(value = "参数")
    private byte[] parameters;

    @ApiModelProperty(value = "优先级")
    private Integer priority;

    @ApiModelProperty(value = "任务类型")
    private Integer taskType;

    @ApiModelProperty(value = "版本号,用乐观锁")
    private Integer version;

    @ApiModelProperty(value = "状态 0=初始化状态 1=EXECUTED 2=CANCELLED")
    private Integer status;


}
