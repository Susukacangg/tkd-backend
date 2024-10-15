package com.tkd.dictionaryservice.service;

import com.tkd.dictionaryservice.dto.LogoutResponse;
import com.tkd.dictionaryservice.dto.UserSession;
import com.tkd.models.LoginRequest;
import com.tkd.models.LoginResponse;
import com.tkd.models.RegistrationRequest;
import com.tkd.models.UserAccount;
import jakarta.servlet.http.Cookie;

public interface IamService {
    String registerUser(RegistrationRequest regisReq) throws Exception;

    UserSession loginUser(LoginRequest loginReq) throws Exception;

    LogoutResponse logoutUser();

    LoginResponse refreshToken(Cookie cookie);

    UserAccount getUserAccount(String token);

    Boolean checkUsernameAvailable(String username);

    Boolean checkEmailAvailable(String email);
}
