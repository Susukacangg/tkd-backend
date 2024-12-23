package com.tkd.iamservice.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseCookie;

@Data
@Builder
public class AuthResponseDto {
    private String message;
    private ResponseCookie tokenCookie;
    private ResponseCookie refreshCookie;
}
