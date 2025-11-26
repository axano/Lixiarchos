package com.web.Lixiarchos.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class IpRateLimitFilter extends OncePerRequestFilter {

    @Value("${app.rateLimit.maxRequests}")
    private int maxRequests;

    @Value("${app.rateLimit.windowSeconds}")
    private long windowSeconds;

    private static class Counter {
        volatile long windowStartEpochSec;
        final AtomicInteger count = new AtomicInteger(0);
    }

    // Leak-proof Caffeine cache:
    private final Cache<String, Counter> ipCounters =
            Caffeine.newBuilder()
                    .expireAfterWrite(5, TimeUnit.MINUTES)    // evict unused entries
                    .maximumSize(100_000)                     // absolute safety cap
                    .build();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String ip = extractClientIp(request);
        long now = Instant.now().getEpochSecond();

        Counter counter = ipCounters.get(ip, k -> {
            Counter c = new Counter();
            c.windowStartEpochSec = now;
            c.count.set(0);
            return c;
        });

        synchronized (counter) {
            if (now - counter.windowStartEpochSec >= windowSeconds) {
                counter.windowStartEpochSec = now;
                counter.count.set(0);
            }

            int current = counter.count.incrementAndGet();
            if (current > maxRequests) {
                // rate limit hit
                response.setStatus(429);
                response.setContentType(MediaType.TEXT_PLAIN_VALUE);
                response.getWriter().write("Slow down buddy!");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractClientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
