package com.tkd.dictionaryservice.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseCookie;

@Data
@Builder
public class LogoutResponse {
    private String message;
    private ResponseCookie responseCookie;
}
