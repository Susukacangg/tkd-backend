package com.tkd.dictionaryservice.service;

import com.tkd.dictionaryservice.dto.UserSession;
import com.tkd.models.AuthResponse;
import com.tkd.models.LoginRequest;
import com.tkd.models.RegistrationRequest;

public interface IamService {
    AuthResponse registerUser(RegistrationRequest regisReq) throws Exception;

    UserSession loginUser(LoginRequest loginReq) throws Exception;

    Boolean checkUsernameAvailable(String username);

    Boolean checkEmailAvailable(String email);
}
