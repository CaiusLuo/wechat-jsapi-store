package com.example.wechatstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AdminTokenAuthenticationFilter adminTokenAuthenticationFilter;

    public SecurityConfig(AdminTokenAuthenticationFilter adminTokenAuthenticationFilter) {
        this.adminTokenAuthenticationFilter = adminTokenAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(adminTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(401, "admin login required"))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendError(403, "forbidden"))
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/api/admin/login",
                                "/api/h5/**",
                                "/api/wx/**",
                                "/api/pay/notify",
                                "/uploads/**",
                                "/doc.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers("/api/admin/**").authenticated()
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}
