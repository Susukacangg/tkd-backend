package com.tkd.iamservice.config;

import com.tkd.iamservice.service.JwtService;
import com.tkd.iamservice.utility.IamServiceUtility;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        Cookie[] cookies = request.getCookies();
        Cookie accessTokenCookie = null;

        // find the access token cookie
        if (cookies != null)
            for (Cookie cookie : cookies)
                if (cookie.getName().equals(IamServiceUtility.TOKEN_COOKIE_KEY))
                    accessTokenCookie = cookie;

        // if dun have token
        if(accessTokenCookie == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwtToken, username;
            jwtToken = accessTokenCookie.getValue();
            username = jwtService.extractUsername(jwtToken);

            // second statement checks if user is not yet authenticated
            // tapi ada la tu user yang 'kunun' mau connect (first statement)
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if(jwtService.isTokenValid(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (ExpiredJwtException e) {
            log.error(e.getMessage());
        } finally {
            filterChain.doFilter(request, response);
        }

    }
}
