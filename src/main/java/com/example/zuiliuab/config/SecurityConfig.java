package com.example.zuiliuab.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 禁用CSRF保护
        http.csrf(csrf -> csrf.disable());
        
        // 允许所有请求访问
        http.authorizeHttpRequests(authz -> authz
            .requestMatchers("/**").permitAll());
        
        // 禁用默认登录页面
        http.formLogin(form -> form.disable());
        http.httpBasic(basic -> basic.disable());
        
        // 禁用记住我功能
        http.rememberMe(remember -> remember.disable());
        
        return http.build();
    }
}