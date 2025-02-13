package com.heima.wemedia.interceptor;

import com.alibaba.fastjson.JSON;
import com.heima.model.common.pojos.CurrentUser;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.AppJwtUtil;
import com.heima.utils.common.WmThreadLocalUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.naming.factory.BeanFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;


@Slf4j
public class WmTokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

//        String userId = request.getHeader("userId");
        String token = request.getHeader("Token");
        Optional<String> optional = Optional.ofNullable(token);
        if (optional.isPresent()) {
            //把用户id存入threadloacl中
            Claims claims = AppJwtUtil.getClaimsBody(token);
            String userStr = (String) claims.get(AppJwtUtil.CURRENT_USER);
            CurrentUser user = JSON.parseObject(userStr,CurrentUser.class);
            if(user != null ) {
                WmUser wmUser = new WmUser();
                BeanUtils.copyProperties(user, wmUser);
                WmThreadLocalUtil.setUser(wmUser);
                log.info("wmTokenFilter设置用户信息到threadlocal中...");
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("清理threadlocal...");
        WmThreadLocalUtil.clear();
    }
}
