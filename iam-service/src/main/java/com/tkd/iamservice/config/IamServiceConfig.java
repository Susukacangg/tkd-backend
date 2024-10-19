package com.tkd.iamservice.config;

import com.tkd.iamservice.repository.UserDao;
import com.tkd.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
@RequiredArgsConstructor
public class IamServiceConfig {
    private final UserDao userDao;

    @Bean
    public CustomUserDetailsService customUserDetailsService() {
        return username -> userDao.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("CustomUserDetailsService: User %s not found", username)));
    }
}
