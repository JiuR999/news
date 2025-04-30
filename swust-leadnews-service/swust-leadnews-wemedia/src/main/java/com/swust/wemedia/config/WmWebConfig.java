package com.swust.wemedia.config;

import com.swust.wemedia.interceptor.WmTokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//@Configuration
public class WmWebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new WmTokenInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/api/v1/news/update"
                        , "/swagger-resources/**"
                        , "/webjars/**"
                        , "/doc.html"
                        , "/swagger-ui.html/**"
                        , "/v2/**"
                        , "/api/v1/material/upload"
                        , "/api/v1/news/audit"
                        , "/api/v1/user/add");
    }

}
