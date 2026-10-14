package com.saisony.jobtracker.config;

import com.saisony.jobtracker.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.Duration;
import java.security.Principal;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    public RateLimitInterceptor(
            RateLimitService rateLimitService) {

        this.rateLimitService = rateLimitService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws IOException {

        Principal principal = request.getUserPrincipal();

        if (principal == null) {
            return true;
        }

        String key =
                "rate-limit:user:" + principal.getName();

        boolean allowed = rateLimitService.isAllowed(
                key,
                100,
                Duration.ofMinutes(1)
        );

        if (allowed) {
            return true;
        }

        response.setStatus(429);
        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        response.getWriter().write(
                """
                {
                  "status": 429,
                  "message": "Too many requests"
                }
                """
        );

        return false;
    }
}