package com.example.config;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class XssFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("Content-Security-Policy", "default-src 'self'; frame-ancestors 'none'; base-uri 'self';");

        HttpServletRequest sanitizedRequest = new XssRequestWrapper(request);
        filterChain.doFilter(sanitizedRequest, response);
    }

    private static class XssRequestWrapper extends HttpServletRequestWrapper {

        XssRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getParameter(String name) {
            return clean(super.getParameter(name));
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values == null) {
                return null;
            }
            String[] cleaned = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                cleaned[i] = clean(values[i]);
            }
            return cleaned;
        }

        @Override
        public String getHeader(String name) {
            return clean(super.getHeader(name));
        }

        private String clean(String input) {
            if (input == null) {
                return null;
            }
            String value = input;
            value = value.replace("<", "&lt;");
            value = value.replace(">", "&gt;");
            value = value.replace("\"", "&quot;");
            value = value.replace("'", "&#x27;");
            return value;
        }
    }
}
