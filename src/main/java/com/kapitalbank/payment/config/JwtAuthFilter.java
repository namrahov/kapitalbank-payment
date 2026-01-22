package com.kapitalbank.payment.config;

import com.kapitalbank.payment.service.TokenService;
import com.kapitalbank.payment.service.UserService;
import com.kapitalbank.payment.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.kapitalbank.payment.util.WhiteListApi.JWT_FILTER_WHITELIST;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthFilter(UserService userService, JwtUtil jwtUtil, TokenService tokenService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader(AUTHORIZATION);
        String email;
        String token;

        // Check if Authorization header is present and starts with "Bearer "
        if (isWhitelisted(request) || authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token
        token = authHeader.substring(7);

        // Try to extract email and validate token
        try {
            email = jwtUtil.extractEmail(token);
        } catch (JwtException e) {
            // Handle invalid token (e.g., SignatureException)
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid JWT token");
            return;
        }

        // Proceed with authentication if email is extracted and no authentication exists
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userService.loadUserByUsername(email);
            if (Boolean.TRUE.equals(jwtUtil.isTokenValid(token, userDetails))) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                // Token is not valid for the user
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token is not valid for this user");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isWhitelisted(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();

        for (Map.Entry<String, List<String>> entry : JWT_FILTER_WHITELIST.entrySet()) {
            if (pathMatcher.match(entry.getKey(), requestURI) &&
                    entry.getValue().contains(requestMethod)) {
                return true;
            }
        }
        return false;
    }

}