package com.swust.article.interceptor;

import com.alibaba.fastjson.JSON;
import com.swust.model.common.pojos.CurrentUser;
import com.swust.model.wemedia.pojos.WmUser;
import com.swust.utils.common.AppJwtUtil;
import com.swust.utils.common.WmThreadLocalUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Slf4j
public class ArticleTokenInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
        String token = request.getHeader("current_user");
        Optional<String> optional = Optional.ofNullable(token);
        if (optional.isPresent()) {
            //把用户id存入threadlike中
            try {
                CurrentUser user = JSON.parseObject(token,CurrentUser.class);
                if(user != null ) {
                    WmUser wmUser = new WmUser();
                    BeanUtils.copyProperties(user, wmUser);
                    WmThreadLocalUtil.setUser(wmUser);
                    log.info("ArticleTokenInterceptor设置用户信息到thread local中...");
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
        return true;
//        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//        response.setContentType("application/json");
//        response.setCharacterEncoding("UTF-8");
//        response.getWriter().write("{\"code\":401,\"message\":\"无效的Token\"}");
//        return false;
    }

    @Override
    public void postHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, ModelAndView modelAndView) {
        log.info("清理Thread local...");
        WmThreadLocalUtil.clear();
    }
}
