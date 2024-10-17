package com.tkd.dictionaryservice.controller;

import com.tkd.apis.IamV1Api;
import com.tkd.dictionaryservice.dto.AuthResponse;
import com.tkd.dictionaryservice.service.IamService;
import com.tkd.dictionaryservice.utility.IamServiceUtility;
import com.tkd.models.LoginRequest;
import com.tkd.models.RegistrationRequest;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

        if(requestOptional.isPresent()) {
            HttpServletRequest request = requestOptional.get();
            Cookie[] cookies = request.getCookies();
            AuthResponse refreshResponse = AuthResponse.builder().build();

            // if got cookie, means got refresh token
            if(cookies != null) {
                Cookie refreshTokenCookie = null;

                // find for the refresh token cookie
                for (Cookie cookie : cookies)
                    if(cookie.getName().equals(IamServiceUtility.REFRESH_TOKEN_COOKIE_KEY))
                        refreshTokenCookie = cookie;

                // refresh the access token
                try {
                    refreshResponse = iamService.refreshToken(refreshTokenCookie);
                    return ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, refreshResponse.getTokenCookie().toString())
                            .body(refreshResponse.getMessage());
                } catch (ExpiredJwtException e) {
                    refreshResponse.setMessage("Refresh token expired");
                    return ResponseEntity.internalServerError().body(refreshResponse.getMessage());
                } catch (UsernameNotFoundException e) {
                    refreshResponse.setMessage("Invalid refresh token");
                    log.error(e.getMessage());
                    return ResponseEntity.internalServerError().body(refreshResponse.getMessage());
                }
            } else { // means no cookies, should not be calling in the first place
                log.error("No cookie passed");
                refreshResponse.setMessage("No refresh token found");
                return ResponseEntity.internalServerError().body(refreshResponse.getMessage());
            }
        }

        // don't have http request
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
