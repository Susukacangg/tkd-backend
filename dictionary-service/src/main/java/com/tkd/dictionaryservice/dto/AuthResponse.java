package com.tkd.dictionaryservice.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseCookie;

@Data
@Builder
public class AuthResponse {
    private String message;
    private ResponseCookie tokenCookie;
    private ResponseCookie refreshCookie;
}
