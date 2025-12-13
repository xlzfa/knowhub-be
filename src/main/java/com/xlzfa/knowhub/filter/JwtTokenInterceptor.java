package com.xlzfa.knowhub.filter;

import com.xlzfa.knowhub.config.JwtProperties;
import com.xlzfa.knowhub.util.BaseContext;
import com.xlzfa.knowhub.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtTokenInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        try {
            //放行静态资源
            if(! (handler instanceof HandlerMethod)){
                return true;
            }
            //拿token
            String token = request.getHeader("token");
            //解析
            Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
            System.out.println(claims.get("id"));
            //拿id，存线程
            Long id = Long.valueOf(claims.get("id").toString());
            BaseContext.setCurrentId(id);

            return true;
        } catch (Exception e) {

            response.setStatus(401);
            return false;

        }
    }
}
