package com.swust.model.wemedia.dtos;

import lombok.Data;

@Data
public class WmAuditDto {
    //资料id
    private Long id;
    //审核意见
    private Short status;
    //拒绝理由
    private String reason;
}
