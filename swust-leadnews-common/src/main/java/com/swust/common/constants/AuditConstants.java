package com.swust.common.constants;

public class AuditConstants {
    /**
     * 草稿
     */
    public static final Short DRAFT = 0;
    /**
     * 待审核
     */
    public static final Short SHOULD_AUDIT = 1;
    /**
     * 审核失败
     */
    public static final Short FAILED_TO_AUDIT = 2;
    /**
     * 人工审核
     */
    public static final Short MANUAL_AUDIT = 3;
    /**
     * 审核成功
     */
    public static final Short SUCCESS_TO_AUDIT = 4;
    /**
     * 待发布
     */
    public static final Short SHOULD_PUBLISH = 8;
    /**
     * 已发布
     */
    public static final Short PUBLISHED = 9;
    /**
     * 发布失败
     */
    public static final Short FAILED_TO_PUBLISH = -1;
}