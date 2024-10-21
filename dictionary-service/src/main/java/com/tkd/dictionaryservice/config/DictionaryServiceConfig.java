package com.tkd.dictionaryservice.config;

import com.tkd.dictionaryservice.dto.IamUserDataDto;
import com.tkd.dictionaryservice.dto.IamUserDto;
import com.tkd.dictionaryservice.dto.UserRole;
import com.tkd.dictionaryservice.feign.IamFeignService;
import com.tkd.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
@RequiredArgsConstructor
public class DictionaryServiceConfig {
    @Value("${security.internal-request.secret}")
    private String internalRequestSecret;

    private final IamFeignService iamFeignService;

    @Bean
    public CustomUserDetailsService customUserDetailsService() {
        return username -> {
            IamUserDataDto iamUserData = iamFeignService.getIamUserDetails(internalRequestSecret, username);
            if (iamUserData != null) {
                return IamUserDto.builder()
                        .username(iamUserData.getUsername())
                        .password(iamUserData.getPassword())
                        .role(UserRole.valueOf(iamUserData.getRole()))
                        .build();
            }

            throw new UsernameNotFoundException(String.format("Username %s not found", username));
        };
    }
}
