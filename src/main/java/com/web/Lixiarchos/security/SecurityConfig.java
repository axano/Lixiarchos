package com.web.Lixiarchos.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.user.username}")
    private String userUsername;

    @Value("${app.user.password}")
    private String userPassword;

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.withUsername(adminUsername)
                .password("{noop}" + adminPassword)
                .roles("ADMIN")
                .build();

        UserDetails user = User.withUsername(userUsername)
                .password("{noop}" + userPassword)
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(admin, user);
    }

    /**
     * Generates a per-request CSP nonce and sets the Content-Security-Policy header
     */
    @Bean
    public Filter cspNonceFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {

                // Generate a random nonce
                String nonce = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
                request.setAttribute("cspNonce", nonce);

                // Set the CSP header
                String csp = "default-src 'self'; " +
                        "script-src 'self' 'nonce-" + nonce + "' https://cdn.jsdelivr.net https://code.jquery.com https://cdnjs.cloudflare.com; " +
                        "style-src 'self' 'nonce-" + nonce + "' 'unsafe-hashes' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
                        "style-src-elem 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
                        "font-src 'self' https://cdn.jsdelivr.net;";

                response.setHeader("Content-Security-Policy", csp);

                filterChain.doFilter(request, response);
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .headers(headers -> headers
                        .xssProtection(xss -> xss
                                .headerValue(org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**","/js/**","/images/**").permitAll()
                        .requestMatchers("/", "/login").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults())
                .logout(Customizer.withDefaults());

        return http.build();
    }
}
