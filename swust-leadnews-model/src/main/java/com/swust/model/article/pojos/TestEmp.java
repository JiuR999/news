package com.swust.model.article.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("ap_emp")
@Data
public class TestEmp {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField()
    private String name;
    @TableField("dep_id")
    private Long depId;
}

