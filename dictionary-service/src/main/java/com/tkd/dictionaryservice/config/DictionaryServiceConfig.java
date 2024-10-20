package com.tkd.dictionaryservice.config;

import com.tkd.dictionaryservice.dto.IamUserDetails;
import com.tkd.dictionaryservice.entity.IamUser;
import com.tkd.dictionaryservice.entity.UserRole;
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
            IamUserDetails iamUserDetails = iamFeignService.getIamUserDetails(internalRequestSecret, username);
            if (iamUserDetails != null) {
                return IamUser.builder()
                        .username(iamUserDetails.getUsername())
                        .password(iamUserDetails.getPassword())
                        .role(UserRole.valueOf(iamUserDetails.getRole()))
                        .build();
            }

            throw new UsernameNotFoundException(String.format("Username %s not found", username));
        };
    }
}
