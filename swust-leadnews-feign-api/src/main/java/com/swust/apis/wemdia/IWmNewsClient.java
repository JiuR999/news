package com.swust.apis.wemdia;

import com.swust.model.common.dtos.ResponseResult;
import com.swust.model.wemedia.dtos.WmNewsDto;
import com.swust.model.wemedia.pojos.WmUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "swust-wemedia")
public interface IWmNewsClient {

    @PostMapping("/api/v1/news/update")
    ResponseResult updateById(@RequestBody WmNewsDto dto);

    @PostMapping("/api/v1/user/add")
    ResponseResult addUser(@RequestBody WmUser dto);
}
