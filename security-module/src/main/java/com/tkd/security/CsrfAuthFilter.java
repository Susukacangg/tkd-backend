package com.tkd.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CsrfAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        if(HttpMethod.GET.matches(request.getMethod()) || request.getRequestURI().startsWith("/api/auth/refresh")) {
            filterChain.doFilter(request, response);
            return;
        }

        String csrfToken = request.getHeader(SecurityUtility.CSRF_TOKEN_HEADER_KEY);
        Cookie[] cookies = request.getCookies();
        Cookie csrfCookie = null;

        if(cookies != null)
            for (Cookie cookie : cookies)
                if (cookie.getName().equals("XSRF-TOKEN"))
                    csrfCookie = cookie;

        if(csrfCookie == null || csrfToken == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"No CSRF token found!\"}");
            response.getWriter().flush();
            return;
        }

        if(!(csrfCookie.getValue().equals(csrfToken))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Invalid CSRF token!\"}");
            response.getWriter().flush();
            return;
        }

        filterChain.doFilter(request, response);
    }
}
