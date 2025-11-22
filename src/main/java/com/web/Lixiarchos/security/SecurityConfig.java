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

        // {noop} tells Spring Security that the password is stored in plain text
        UserDetails admin = User.withUsername(adminUsername)
                .password("{noop}" + adminPassword)
                .roles("ADMIN")
                .build();

        UserDetails user = User.withUsername(userUsername)
                .password("{noop}"+ userPassword)
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(admin, user);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                //.csrf(csrf -> csrf.enable()) // CSRF protection is enabled by default in Spring Security
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
