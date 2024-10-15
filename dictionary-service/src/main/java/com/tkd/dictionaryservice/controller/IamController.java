package com.tkd.dictionaryservice.controller;

import com.tkd.apis.IamV1Api;
import com.tkd.dictionaryservice.dto.LogoutResponse;
import com.tkd.dictionaryservice.dto.UserSession;
import com.tkd.dictionaryservice.service.IamService;
import com.tkd.dictionaryservice.utility.IamServiceUtility;
import com.tkd.models.LoginRequest;
import com.tkd.models.LoginResponse;
import com.tkd.models.RegistrationRequest;
import com.tkd.models.UserAccount;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;
import java.util.Optional;

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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
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
    public ResponseEntity<LoginResponse> refreshToken() {
        Optional<HttpServletRequest> requestOptional = getRequest();
        LoginResponse loginResponse = new LoginResponse();

        if(requestOptional.isPresent()) {
            HttpServletRequest request = requestOptional.get();
            Cookie[] cookies = request.getCookies();

            // if got cookie, means got refresh token
            if(cookies != null) {
                Cookie refreshTokenCookie = null;

                // find for the refresh token cookie
                for (Cookie cookie : cookies)
                    if(cookie.getName().equals(IamServiceUtility.REFRESH_TOKEN_COOKIE_KEY))
                        refreshTokenCookie = cookie;

                try {
                    return ResponseEntity.ok(iamService.refreshToken(refreshTokenCookie));
                } catch (ExpiredJwtException e) {
                    loginResponse.setMessage("Refresh token expired");
                    return ResponseEntity.internalServerError().body(loginResponse);
                }
            } else { // means no cookies, should not be calling in the first place
                log.error("No cookie present");
                loginResponse.setMessage("No refresh token found");
                return ResponseEntity.internalServerError().body(loginResponse);
            }
        }

        // don't have http request
        return ResponseEntity.internalServerError().body(null);
    }

    @Override
    public ResponseEntity<UserAccount> getUserDetails() {
        Optional<HttpServletRequest> requestOptional = getRequest();
        if(requestOptional.isPresent()) {
            HttpServletRequest request = requestOptional.get();
            // "Bearer " <- has seven characters
            String token = request.getHeader("Authorization").substring(7);

            return ResponseEntity.ok(iamService.getUserAccount(token));
        }

        return ResponseEntity.internalServerError().body(null);
    }

    @Override
    public ResponseEntity<Boolean> checkUsernameAvailable(String username) {
        return ResponseEntity.ok(iamService.checkUsernameAvailable(username));
    }

    @Override
    public ResponseEntity<Boolean> checkEmailAvailable(String email) {
        return ResponseEntity.ok(iamService.checkEmailAvailable(email));
    }

    @Override
    public Optional<HttpServletRequest> getRequest() {
        return Optional.of(
                ((ServletRequestAttributes)
                        Objects.requireNonNull(RequestContextHolder.getRequestAttributes()))
                        .getRequest()
        );
    }
}
