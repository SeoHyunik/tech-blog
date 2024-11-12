package com.automatic.tech_blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(
            authorize -> authorize.requestMatchers("/", "/login")
                .permitAll()
                .anyRequest()
                .authenticated())
            .oauth2Login(oauth2Login -> oauth2Login.defaultSuccessUrl("/drive-files", true));
        return http.build();
    }
}
