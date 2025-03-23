package com.swust.model.common.pojos;

import lombok.Data;

@Data
public class StatisticModel {
    /**
     * 统计指标名称
     */
    private String name;
    /**
     * 统计结果值
     */
    private String value;
}
