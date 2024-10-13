package com.tkd.dictionaryservice.dto;

import com.tkd.models.LoginResponse;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseCookie;

@Data
@Builder
public class UserSession {
    private LoginResponse loginResponse;
    private ResponseCookie responseCookie;
}
