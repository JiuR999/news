package com.swust.wemedia.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.swust.common.constants.KafkaMessageConstants;
import com.swust.model.wemedia.pojos.WmNews;
import com.swust.wemedia.mapper.WmNewsMapper;
import io.jsonwebtoken.lang.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;

@Slf4j
@Component
public class ArticlePublishListener {

    @Autowired
    WmNewsMapper wmNewsMapper;
    @KafkaListener(topics = KafkaMessageConstants.WM_NEWS_PUBLISH_TOPIC)
    public void onMessage(String msg) {
        log.info("收到资料{}发布消息", msg);
        if (msg != null && !msg.equals("")) {
            JSONObject jsonObject = JSON.parseObject(msg);
            WmNews wmNew = new WmNews();
            wmNew.setId(Integer.valueOf((String) jsonObject.get("id")));
            wmNew.setArticleId(Long.valueOf((String) jsonObject.get("articleId")));
            wmNew.setPublishTime(LocalDateTime.now());
            wmNew.setStatus((short) 9);
            wmNewsMapper.updateById(wmNew);
        }
    }
}
