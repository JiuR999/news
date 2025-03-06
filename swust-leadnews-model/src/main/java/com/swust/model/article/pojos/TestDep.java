package com.swust.model.article.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

@TableName("ap_dep")
@Data
public class TestDep {
    @TableId(type = IdType.AUTO)
    Long id;
    @TableField
    String name;
    List<TestEmp> emps;
}
