package com.saisony.jobtracker.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger =
            LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String requestId =
                UUID.randomUUID().toString();

        long startTime =
                System.nanoTime();

        MDC.put(
                "requestId",
                requestId
        );

        response.setHeader(
                "X-Request-Id",
                requestId
        );

        try {

            filterChain.doFilter(
                    request,
                    response
            );

        } finally {

            long durationMs =
                    (System.nanoTime() - startTime)
                            / 1_000_000;

            logger.info(
                    "request completed method={} path={} status={} durationMs={} requestId={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs,
                    requestId
            );

            MDC.remove(
                    "requestId"
            );
        }
    }
}