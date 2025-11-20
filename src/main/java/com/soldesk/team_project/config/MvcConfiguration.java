package com.soldesk.team_project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.soldesk.team_project.security.AuthInterceptor;

import java.util.concurrent.TimeUnit;

@Configuration
public class MvcConfiguration implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public MvcConfiguration(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/templates/", "classpath:/static/")
                .setCacheControl(CacheControl.maxAge(10, TimeUnit.MINUTES));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**") // 모든 경로에 적용
                .excludePathPatterns(
                        "/css/**", "/js/**", "/img/**", // 정적 리소스 제외
                        "/error", // 에러 페이지 제외
                        "/member/login", "/member/join/**", // 로그인/회원가입 제외
                        "/oauth2/**" // OAuth 인증 경로 제외
                );
    }
}
