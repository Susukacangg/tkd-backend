package com.tkd.iamservice.service;

import com.tkd.iamservice.dto.AuthResponse;
import com.tkd.models.LoginRequest;
import com.tkd.models.RegistrationRequest;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface IamService {
    AuthResponse registerUser(RegistrationRequest regisReq) throws Exception;

    AuthResponse loginUser(LoginRequest loginReq) throws Exception;

    AuthResponse logoutUser();

    AuthResponse refreshToken(Cookie cookie) throws ExpiredJwtException, UsernameNotFoundException;

    Boolean checkUsernameAvailable(String username);

    Boolean checkEmailAvailable(String email);
}
