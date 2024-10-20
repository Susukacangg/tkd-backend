package com.tkd.iamservice.controller;

import com.tkd.apis.IamV1Api;
import com.tkd.iamservice.dto.AuthResponse;
import com.tkd.iamservice.service.IamService;
import com.tkd.iamservice.utility.IamServiceUtility;
import com.tkd.models.IamUserDetails;
import com.tkd.models.LoginRequest;
import com.tkd.models.RegistrationRequest;
import com.tkd.models.UserAccount;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
        AuthResponse registerResponse = AuthResponse.builder().build();

        try {
            registerResponse = iamService.registerUser(body);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, registerResponse.getTokenCookie().toString())
                    .header(HttpHeaders.SET_COOKIE, registerResponse.getRefreshCookie().toString())
                    .body(registerResponse.getMessage());
        } catch (Exception e) {
            registerResponse.setMessage("User already exists!");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(registerResponse.getMessage());
        }
    }

    @Override
    public ResponseEntity<String> loginUser(LoginRequest body) {
        AuthResponse loginResponse = AuthResponse.builder().build();
        try {
            loginResponse = iamService.loginUser(body);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, loginResponse.getTokenCookie().toString())
                    .header(HttpHeaders.SET_COOKIE, loginResponse.getRefreshCookie().toString())
                    .body(loginResponse.getMessage());
        } catch (Exception e) {
            loginResponse.setMessage("The login ID or password provided is incorrect");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(loginResponse.getMessage());
        }
    }

    @Override
    public ResponseEntity<String> logoutUser() {
        AuthResponse logoutResponse = iamService.logoutUser();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, logoutResponse.getTokenCookie().toString())
                .header(HttpHeaders.SET_COOKIE, logoutResponse.getRefreshCookie().toString())
                .body(logoutResponse.getMessage());
    }

    @Override
    public ResponseEntity<String> refreshToken() {
        Optional<HttpServletRequest> requestOptional = getRequest();

        // don't have http request
        if(requestOptional.isEmpty())
            return ResponseEntity.internalServerError().body(null);

        HttpServletRequest request = requestOptional.get();
        Cookie[] cookies = request.getCookies();
        AuthResponse refreshResponse = AuthResponse.builder().build();
        refreshResponse.setMessage("Refresh token error");

        // NO COOKIES, should not be calling in the first place
        if(cookies == null) {
            log.error("No cookie passed");
            return ResponseEntity.internalServerError().body(refreshResponse.getMessage());
        }

        // find for the refresh token cookie
        Cookie refreshTokenCookie = null;
        for (Cookie cookie : cookies)
            if(cookie.getName().equals(IamServiceUtility.REFRESH_TOKEN_COOKIE_KEY))
                refreshTokenCookie = cookie;

        // NO REFRESH TOKEN
        if(refreshTokenCookie == null) {
            log.error("No refresh token cookie");
            return ResponseEntity.internalServerError().body(refreshResponse.getMessage());
        }

        // refresh the access token
        try {
            refreshResponse = iamService.refreshToken(refreshTokenCookie);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshResponse.getTokenCookie().toString())
                    .body(refreshResponse.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body(refreshResponse.getMessage());
        }
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
    public ResponseEntity<UserAccount> getUserDetails(Boolean includeId) {
        Optional<HttpServletRequest> requestOptional = getRequest();
        log.info("get user details");

        if(requestOptional.isEmpty())
            return ResponseEntity.internalServerError().body(null);

        HttpServletRequest request = requestOptional.get();
        Cookie[] cookies = request.getCookies();

        if(cookies == null) {
            log.error("No cookies passed getting user details");
            return ResponseEntity.internalServerError().body(null);
        }

        Cookie tokenCookie = null;
        for(Cookie cookie : cookies) {
            if(cookie.getName().equals(IamServiceUtility.TOKEN_COOKIE_KEY))
                tokenCookie = cookie;
        }

        if(tokenCookie == null) {
            log.error("No token cookie passed getting user details");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            return ResponseEntity.ok(iamService.getUserDetails(tokenCookie.getValue(), includeId));
        } catch (AccountExpiredException | IllegalArgumentException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (UsernameNotFoundException e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @Override
    public ResponseEntity<IamUserDetails> getIamUserDetails(String xInternalCall, String loginId) {
        try {
            return ResponseEntity.ok(iamService.getIamUserDetails(loginId));
        } catch (UsernameNotFoundException e) {
            log.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
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
