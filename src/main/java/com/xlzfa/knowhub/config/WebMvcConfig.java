package com.xlzfa.knowhub.config;

import com.xlzfa.knowhub.filter.JwtTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtTokenInterceptor jwtTokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtTokenInterceptor)
                .addPathPatterns("/**")       // 拦截所有请求
                .excludePathPatterns("/user/login","/v3/api-docs/**","/swagger-ui/**"); // 登录接口放行,swagger放行

    }


    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")               // 所有接口都允许跨域
                        .allowedOrigins("http://localhost:5173") // 只允许前端端口访问
                        .allowedMethods("*")             // GET, POST, PUT, DELETE 等全部允许
                        .allowCredentials(true);        // 允许发送 Cookie
            }
        };
    }

}
