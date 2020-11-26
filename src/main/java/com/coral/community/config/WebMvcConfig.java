package com.coral.community.config;

import com.coral.community.controller.interceptor.AlphaInterceptor;
import com.coral.community.controller.interceptor.LoginTicketInterceptor;
import com.coral.community.entity.LoginTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private AlphaInterceptor alphaInterceptor;

    @Autowired
    private LoginTicketInterceptor loginTicketInterceptor;

    //register for the intercepter
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(alphaInterceptor)
                .excludePathPatterns("/css/*.css","/js/*.js","/img/*.png","/img/*.jpg","/img/*.jpeg")
                .addPathPatterns("/register","/login");
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/css/*.css","/js/*.js","/img/*.png","/img/*.jpg","/img/*.jpeg");

    }


}