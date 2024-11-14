package com.tkd.dictionaryservice.config;

import com.tkd.security.CsrfAuthFilter;
import com.tkd.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationProvider authProvider;
    private final CsrfAuthFilter csrfAuthFilter;

    private final String[] URL_BLACKLIST = {
            "/dict/add",
            "/dict/get-users-words",
            "/report-contribution"
    };

    private final String[] URL_BLACKLIST_ALL = {
            "/swagger-ui/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(matcherRegistry -> matcherRegistry
                        .requestMatchers(URL_BLACKLIST).authenticated()
                        .requestMatchers(HttpMethod.PUT, "/dict/{wordId}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/dict/{wordId}").authenticated()
                        .requestMatchers(URL_BLACKLIST_ALL).denyAll()
                        .anyRequest().permitAll())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(csrfAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
