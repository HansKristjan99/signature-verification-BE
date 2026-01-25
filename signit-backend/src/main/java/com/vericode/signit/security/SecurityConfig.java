package com.vericode.signit.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private SessionValidationFilter sessionValidationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS configuration - MUST be specific for cookies/CSRF to work.
     * You cannot use "*" when allowCredentials is true.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // CHANGE THIS: You must list your exact frontend URL.
        // "*" is not allowed when allowCredentials is true.
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "https://your-production-domain.com"));
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // IMPORTANT: Allow the frontend to see the CSRF token
        configuration.setExposedHeaders(Arrays.asList("X-Session-Token", "X-XSRF-TOKEN"));
        
        // IMPORTANT: This allows Cookies (Session & CSRF) to be sent/received
        configuration.setAllowCredentials(true); 
        
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. Enable CORS with the strict config above
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 2. Enable CSRF with Cookie repository
            .csrf(csrf -> csrf
                // Store the token in a cookie named "XSRF-TOKEN"
                // withHttpOnlyFalse() is CRITICAL so your JS can read it
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                // Ensure the token is available to SPAs immediately
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                .ignoringRequestMatchers("/users/login", "/users/register")
            )
            
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/users/login", "/users/register", "/error").permitAll()
                .anyRequest().authenticated()
            ) 
            
            // 3. Security Headers (Recommended defaults, tweaked)
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny())
            )
            
            .addFilterBefore(sessionValidationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}