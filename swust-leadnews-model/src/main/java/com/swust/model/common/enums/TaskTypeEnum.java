package com.swust.model.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskTypeEnum {

    NEWS_PUBLISH_TIME(1001, 1,"文章定时发布"),
    NEWS_AUDIT_TIME(1002, 2,"文章定时审核"),
    FILE_AUDIT_TIME(1003, 3,"文件定时审核"),
    REMOTEERROR(1003, 3,"第三方接口调用失败，重试");
    private final int taskType; //对应具体业务
    private final int priority; //业务不同级别
    private final String desc; //描述信息
}