package com.tkd.iamservice.service;

import com.tkd.iamservice.dto.AuthResponseDto;
import com.tkd.models.IamUserData;
import com.tkd.models.LoginRequest;
import com.tkd.models.RegistrationRequest;
import com.tkd.models.UserView;
import jakarta.servlet.http.Cookie;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface IamService {
    AuthResponseDto registerUser(RegistrationRequest regisReq) throws Exception;

    AuthResponseDto loginUser(LoginRequest loginReq) throws UsernameNotFoundException, AuthenticationException;

    AuthResponseDto logoutUser();

    AuthResponseDto refreshToken(Cookie cookie) throws UsernameNotFoundException, IllegalStateException, AccountExpiredException;

    Boolean checkUsernameAvailable(String username);

    Boolean checkEmailAvailable(String email);

    UserView getUserDetails(String token, boolean includeId);

    IamUserData getIamUserDetails(String loginId) throws UsernameNotFoundException;

    Boolean adminCheck(String token);
}
