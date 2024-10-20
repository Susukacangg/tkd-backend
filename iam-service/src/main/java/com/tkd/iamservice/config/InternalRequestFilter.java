package com.tkd.iamservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class InternalRequestFilter extends OncePerRequestFilter {
    @Value("${security.internal-request.secret}")
    private String internalRequestSecret;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {


        if (request.getRequestURI().startsWith("/api/internal")) {
            String internalRequestHeader = request.getHeader("X-internal-call");
            if (internalRequestHeader == null || !internalRequestHeader.equals(internalRequestSecret)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden: Internal access only");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
