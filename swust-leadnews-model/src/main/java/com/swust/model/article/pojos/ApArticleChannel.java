package com.swust.model.article.pojos;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApArticleChannel {

    /**
     * 频道id
     */
    @TableField("channel_id")
    private Long channelId;

    /**
     * 频道名称
     */
    @TableField("channel_name")
    private String channelName;
}
