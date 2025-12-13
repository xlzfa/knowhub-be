package com.xlzfa.knowhub.config;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class FastJsonConfigurer implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 1. 创建 FastJsonHttpMessageConverter
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();

        // 2. 配置 FastJsonConfig
        FastJsonConfig config = new FastJsonConfig();
        config.setSerializerFeatures(
                SerializerFeature.PrettyFormat,           // 格式化输出
                SerializerFeature.WriteMapNullValue,     // 输出null字段
                SerializerFeature.WriteDateUseDateFormat // Date格式化
        );
        config.setDateFormat("yyyy-MM-dd HH:mm:ss");  // 全局日期格式
        fastConverter.setFastJsonConfig(config);
        fastConverter.setDefaultCharset(StandardCharsets.UTF_8);

        // 3. 支持的媒体类型
        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.APPLICATION_JSON);
        mediaTypes.add(MediaType.APPLICATION_JSON_UTF8); // Spring Boot 2.x 可用
        fastConverter.setSupportedMediaTypes(mediaTypes);

        // 4. 将 FastJsonHttpMessageConverter 添加到 converters
        converters.add(0, fastConverter); // 放在最前，优先使用
    }
}
