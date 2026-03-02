package com.jb.urlShortner.urlShortner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/ping", "/error", "/oauth2/**", "/login/**", "/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/urls/my", "/metrics/summary").authenticated()
                        .requestMatchers(HttpMethod.GET, "/myUrls", "/my-urls").authenticated()
                        .requestMatchers(HttpMethod.GET, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/shorten").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/auth/success", true)
                        .failureHandler(new SimpleUrlAuthenticationFailureHandler("/auth/failure"))
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) -> response.setStatus(200))
                );

        return http.build();
    }
}
