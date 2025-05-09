package com.swust.app.gateway.filter;


import com.swust.app.gateway.util.AppJwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class AuthorizeFilter implements Ordered, GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1.获取request和response对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        //2.判断是否是登录或注册
        if (request.getURI().getPath().contains("/login") || request.getURI().getPath().contains("/user/add")) {
            log.info("用户Login或Register");
            //放行
            return chain.filter(exchange);
        }

        //3.获取token
        String token = request.getHeaders().getFirst("Token");

        //4.判断token是否存在
        if (StringUtils.isBlank(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            log.info("用户未登录访问{}", request.getURI().getPath());
            return response.setComplete();
        }

        //5.判断token是否有效
        try {
            Claims claimsBody = AppJwtUtil.getClaimsBody(token);
            //是否是过期
            int result = AppJwtUtil.verifyToken(claimsBody);
            if (result == 1 || result == 2) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
            if (result == -1) {
                String newToken = AppJwtUtil.getToken(AppJwtUtil.getClaimsBody(token));
                log.info("续期{}", newToken);
            }
            String userStr = (String) claimsBody.get("current_user");
            // 创建新的请求对象并替换原来的请求对象
            ServerHttpRequest.Builder builder = request.mutate()
                    .header("current_user", userStr);

            ServerHttpRequest newRequest = builder.build();
            exchange = exchange.mutate().request(newRequest).build();
            log.info("用户" + userStr + "登录");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        //6.放行
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            log.info("后置全局过滤器");

        }));
    }

    /**
     * 优先级设置  值越小  优先级越高
     *
     * @return
     */
    @Override
    public int getOrder() {
        return -1;
    }
}
