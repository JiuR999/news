package com.swust.apis.wmuser;

import com.swust.model.article.dtos.ArticleDto;
import com.swust.model.common.dtos.ResponseResult;
import com.swust.model.wemedia.pojos.WmUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(value = "swust-wemedia")
public interface IWmUserClient {

    @PostMapping("/api/v1/user/add")
    ResponseResult addUser(@RequestBody WmUser dto);
}
