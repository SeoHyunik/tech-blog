package com.automatic.tech_blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/", "/login", "/public/**", "/blog/api/v1/google/**").permitAll() // 특정 경로는 인증 없이 접근 가능
                .anyRequest().authenticated() // 나머지 경로는 인증 요구
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/oauth2/authorization/google") // 스프링에서 제공하는 기본 로그인 엔드포인트 사용
                .defaultSuccessUrl("/google-drive", true) // 로그인 성공 후 리다이렉트 경로 설정
            );

        return http.build();
    }
}
