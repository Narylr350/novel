package com.wtl.novel.Config;

import com.wtl.novel.Interceptor.SignatureInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired // 自动注入拦截器 Bean
    private SignatureInterceptor signatureInterceptor;

    @Autowired
    private CorsPolicy corsPolicy;

    @Bean
    public List<String> limitUrl() {
        List<String> urls = new ArrayList<>();
        urls.add("/api/chapters/update");
        urls.add("/api/feedback/add");
        urls.add("/api/comments/add");
        return urls;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(signatureInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**",
                        "/test/**"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (!corsPolicy.hasConfiguredOrigins()) {
            return;
        }

        registry.addMapping("/**") // 仅对显式允许的来源开放跨域访问
                .allowedOriginPatterns(corsPolicy.getAllowedOriginPatterns().toArray(String[]::new))
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
