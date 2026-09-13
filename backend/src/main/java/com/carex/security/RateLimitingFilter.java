package com.carex.security;

import com.carex.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private static final int MAX_REQUESTS_PER_MINUTE = 30;
    private static final long TIME_WINDOW_MS = 60_000L;

    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Long>> requestLog = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Apply rate limiting to sensitive public authentication routes
        if (path.startsWith("/api/auth/login") || path.startsWith("/api/auth/register")) {
            String clientIp = getClientIp(request);
            long now = System.currentTimeMillis();

            ConcurrentLinkedQueue<Long> timestamps = requestLog.computeIfAbsent(clientIp, k -> new ConcurrentLinkedQueue<>());

            // Evict timestamps older than 1 minute
            while (!timestamps.isEmpty() && (now - timestamps.peek() > TIME_WINDOW_MS)) {
                timestamps.poll();
            }

            if (timestamps.size() >= MAX_REQUESTS_PER_MINUTE) {
                log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, path);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                ErrorResponse errorResponse = new ErrorResponse(
                        HttpStatus.TOO_MANY_REQUESTS.value(),
                        "Too Many Requests",
                        "Rate limit exceeded. Please wait a moment before retrying.",
                        path
                );

                response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                return;
            }

            timestamps.add(now);
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
