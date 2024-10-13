package com.tkd.dictionaryservice.service;

import com.tkd.dictionaryservice.dto.LogoutResponse;
import com.tkd.dictionaryservice.dto.UserSession;
import com.tkd.models.LoginRequest;
import com.tkd.models.RegistrationRequest;

public interface IamService {
    String registerUser(RegistrationRequest regisReq) throws Exception;

    UserSession loginUser(LoginRequest loginReq) throws Exception;

    LogoutResponse logoutUser();

    Boolean checkUsernameAvailable(String username);

    Boolean checkEmailAvailable(String email);
}
