package com.swust.model.wemedia.vos;

import com.swust.model.wemedia.pojos.WmNews;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class WmNewVO extends WmNews {
    //文章作者
    private String author;
    //文章分类
    private String channelName;
}
