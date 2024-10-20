package com.tkd.iamservice.service;

import com.tkd.iamservice.dto.AuthResponse;
import com.tkd.models.IamUserDetails;
import com.tkd.models.LoginRequest;
import com.tkd.models.RegistrationRequest;
import com.tkd.models.UserAccount;
import jakarta.servlet.http.Cookie;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface IamService {
    AuthResponse registerUser(RegistrationRequest regisReq) throws Exception;

    AuthResponse loginUser(LoginRequest loginReq) throws Exception;

    AuthResponse logoutUser();

    AuthResponse refreshToken(Cookie cookie) throws UsernameNotFoundException, IllegalStateException, AccountExpiredException;

    Boolean checkUsernameAvailable(String username);

    Boolean checkEmailAvailable(String email);

    UserAccount getUserDetails(String username, boolean includeId) throws UsernameNotFoundException, IllegalArgumentException, AccountExpiredException;

    IamUserDetails getIamUserDetails(String loginId) throws UsernameNotFoundException;
}
