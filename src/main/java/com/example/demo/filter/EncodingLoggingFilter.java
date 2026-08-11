package com.example.demo.filter;

import com.example.demo.config.AppKeys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class EncodingLoggingFilter extends HttpFilter {
    private static final Logger LOG = Logger.getLogger(EncodingLoggingFilter.class.getName());

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        long started = System.nanoTime();
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        AtomicLong requestCount = (AtomicLong) getServletContext().getAttribute(AppKeys.REQUEST_COUNT);
        long number = requestCount.incrementAndGet();
        LOG.info(() -> "Filter 前置 #" + number + " " + request.getMethod() + " " + request.getRequestURI());
        try {
            chain.doFilter(request, response);
        } finally {
            long millis = (System.nanoTime() - started) / 1_000_000;
            LOG.info(() -> "Filter 后置 #" + number + " status=" + response.getStatus() + " " + millis + "ms");
        }
    }
}
