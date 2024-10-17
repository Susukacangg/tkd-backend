package com.tkd.dictionaryservice.service;

import com.tkd.dictionaryservice.dto.AuthResponse;
import com.tkd.models.LoginRequest;
import com.tkd.models.RegistrationRequest;
import com.tkd.models.UserAccount;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface IamService {
    AuthResponse registerUser(RegistrationRequest regisReq) throws Exception;

    AuthResponse loginUser(LoginRequest loginReq) throws Exception;

    AuthResponse logoutUser();

    AuthResponse refreshToken(Cookie cookie) throws ExpiredJwtException, UsernameNotFoundException;

    UserAccount getUserAccount(String token);

    Boolean checkUsernameAvailable(String username);

    Boolean checkEmailAvailable(String email);
}
