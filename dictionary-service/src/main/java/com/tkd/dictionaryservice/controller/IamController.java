package com.tkd.dictionaryservice.controller;

import com.tkd.apis.IamV1Api;
import com.tkd.dictionaryservice.dto.LogoutResponse;
import com.tkd.dictionaryservice.dto.UserSession;
import com.tkd.dictionaryservice.service.IamService;
import com.tkd.models.LoginRequest;
import com.tkd.models.LoginResponse;
import com.tkd.models.RegistrationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class IamController implements IamV1Api {

    private final IamService iamService;

    @Override
    public ResponseEntity<String> registerUser(RegistrationRequest body) {
        try {
            return ResponseEntity.ok(iamService.registerUser(body));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }

    @Override
    public ResponseEntity<LoginResponse> loginUser(LoginRequest body) {
        try {
            UserSession userSession = iamService.loginUser(body);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, userSession.getResponseCookie().toString())
                    .body(userSession.getLoginResponse());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }

    @Override
    public ResponseEntity<String> logoutUser() {
        LogoutResponse logoutResponse = iamService.logoutUser();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, logoutResponse.getResponseCookie().toString())
                .body(logoutResponse.getMessage());
    }

    @Override
    public ResponseEntity<Boolean> checkUsernameAvailable(String username) {
        return ResponseEntity.ok(iamService.checkUsernameAvailable(username));
    }

    @Override
    public ResponseEntity<Boolean> checkEmailAvailable(String email) {
        return ResponseEntity.ok(iamService.checkEmailAvailable(email));
    }
}
