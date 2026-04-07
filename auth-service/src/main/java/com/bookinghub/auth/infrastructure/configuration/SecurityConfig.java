package com.bookinghub.auth.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    return web -> web.ignoring()
        .requestMatchers(new AntPathRequestMatcher("/v3/api-docs/**"))
        .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**"))
        .requestMatchers(new AntPathRequestMatcher("/swagger-ui.html"))
        .requestMatchers(new AntPathRequestMatcher("/webjars/**"))
        .requestMatchers(new AntPathRequestMatcher("/v3/api-docs.yaml"))
        .requestMatchers(new AntPathRequestMatcher("/swagger-resources/**"))
        .requestMatchers(new AntPathRequestMatcher("/favicon.ico"));
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(new AntPathRequestMatcher("/register")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/login")).permitAll()
                .anyRequest().authenticated()
        );
    return http.build();
  }
}
