package com.swust.wemedia.interceptor;

import com.alibaba.fastjson.JSON;
import com.swust.model.common.pojos.CurrentUser;
import com.swust.model.wemedia.pojos.WmUser;
import com.swust.utils.common.AppJwtUtil;
import com.swust.utils.common.WmThreadLocalUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Slf4j
public class WmTokenInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Token");
        Optional<String> optional = Optional.ofNullable(token);
        if (optional.isPresent()) {
            //把用户id存入threadloacl中
            try {
                Claims claims = AppJwtUtil.getClaimsBody(token);
                String userStr = (String) claims.get(AppJwtUtil.CURRENT_USER);
                CurrentUser user = JSON.parseObject(userStr,CurrentUser.class);
                if(user != null ) {
                    WmUser wmUser = new WmUser();
                    BeanUtils.copyProperties(user, wmUser);
                    WmThreadLocalUtil.setUser(wmUser);
                    log.info("wmTokenFilter设置用户信息到threadlocal中...");
                }
                return true;
            } catch (ExpiredJwtException e) {
                // 处理token过期的异常
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"Token已过期\"}");
                return false;
            } catch (Exception e) {
                // 处理其他可能的异常
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"code\":401,\"message\":\"无效的Token\"}");
                return false;
            }

        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"无效的Token\"}");
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("清理threadlocal...");
        WmThreadLocalUtil.clear();
    }
}
