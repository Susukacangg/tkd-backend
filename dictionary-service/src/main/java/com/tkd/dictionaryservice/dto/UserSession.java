package com.tkd.dictionaryservice.dto;

import com.tkd.models.AuthResponse;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseCookie;

@Data
@Builder
public class UserSession {
    private AuthResponse authResponse;
    private ResponseCookie responseCookie;
}
