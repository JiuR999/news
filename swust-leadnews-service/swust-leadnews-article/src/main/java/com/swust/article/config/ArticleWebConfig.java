package com.swust.article.config;

import com.swust.article.interceptor.ArticleTokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ArticleWebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ArticleTokenInterceptor())
                .addPathPatterns("/**")
                //排除登录、注册接口
                .excludePathPatterns("/login/**", "/swagger-resources/**"
                        , "/webjars/**"
                        , "/doc.html"
                        , "/swagger-ui.html/**"
                        , "/v2/**"
                        , "/api/v1/article/submit");
    }

}
