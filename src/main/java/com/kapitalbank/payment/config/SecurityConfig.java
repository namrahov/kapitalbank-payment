package com.kapitalbank.payment.config;

import com.kapitalbank.payment.dao.entity.Permission;
import com.kapitalbank.payment.mapper.TokenMapper;
import com.kapitalbank.payment.service.PermissionService;
import com.kapitalbank.payment.service.TokenService;
import com.kapitalbank.payment.service.UserService;
import com.kapitalbank.payment.util.JwtUtil;
import com.kapitalbank.payment.util.UserUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Set;

import static com.kapitalbank.payment.util.WhiteListApi.AUTH_WHITELIST;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    @Value("${security.cors.origin}")
    private String origin;

    @Value("${security.cors.header}")
    private String header;

    @Value("${security.cors.method}")
    private String method;

    private final JwtAuthFilter jwtAuthFilter;
    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final PermissionService permissionService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          @Lazy UserService userService,
                          BCryptPasswordEncoder bCryptPasswordEncoder,
                          PermissionService permissionService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userService = userService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.permissionService = permissionService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()))
                .headers(headers -> headers
                        .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED))
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                );

        http.csrf(AbstractHttpConfigurer::disable);
        http.sessionManagement(sessionManagement -> sessionManagement
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));


        Set<Permission> permissions = permissionService.findAll();

        http.authorizeHttpRequests(authorizeHttpRequests -> {
            // Permit whitelists first (any HTTP method)
            authorizeHttpRequests.requestMatchers(AUTH_WHITELIST).permitAll();

            // Then add dynamic permissions from DB
            permissions.forEach(permission -> {
                HttpMethod httpMethod = HttpMethod.valueOf(permission.getHttpMethod().toUpperCase());
                String[] roles = permission.getRoles().stream()
                        .map(role -> role.getName().replace("ROLE_", ""))
                        .toArray(String[]::new);

                if (roles.length > 0) {
                    authorizeHttpRequests.requestMatchers(httpMethod, permission.getUrl()).hasAnyRole(roles);
                } else {
                    System.out.println("Skipping permission with empty roles for URL: " + permission.getUrl());
                }
            });

            // Additional specific permits
          //  authorizeHttpRequests.requestMatchers("GET", "/v1/competitions/*").permitAll();
          //  authorizeHttpRequests.requestMatchers("GET", "/v1/datasets/*").permitAll();

            // Deny everything else
            authorizeHttpRequests.anyRequest().denyAll();
        });

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService());
        authenticationProvider.setPasswordEncoder(bCryptPasswordEncoder);

        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManagerBean(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return userService;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin(origin);  // "*" for all origins
        configuration.addAllowedHeader(header);
        configuration.addAllowedMethod(method);
        configuration.setAllowCredentials(false);  // Disable if not using cookies; change to true only with specific origins/patterns
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}