package com.tkd.iamservice.controller;

import com.tkd.apis.IamV1Api;
import com.tkd.iamservice.dto.AuthResponseDto;
import com.tkd.iamservice.service.IamService;
import com.tkd.iamservice.utility.IamServiceUtility;
import com.tkd.models.IamUserData;
import com.tkd.models.LoginRequest;
import com.tkd.models.RegistrationRequest;
import com.tkd.models.UserView;
import com.tkd.security.SecurityUtility;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class IamController implements IamV1Api {

    private final IamService iamService;

    @Override
    public ResponseEntity<String> registerUser(String xXsrfToken, RegistrationRequest body) {
        AuthResponseDto registerResponse = AuthResponseDto.builder().build();

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
    public ResponseEntity<String> loginUser(String xXsrfToken, LoginRequest body) {
        AuthResponseDto loginResponse = AuthResponseDto.builder().build();
        try {
            loginResponse = iamService.loginUser(body);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, loginResponse.getTokenCookie().toString())
                    .header(HttpHeaders.SET_COOKIE, loginResponse.getRefreshCookie().toString())
                    .body(loginResponse.getMessage());
        } catch (UsernameNotFoundException e) {
            loginResponse.setMessage("User not registered");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(loginResponse.getMessage());
        } catch (AuthenticationException e) {
            loginResponse.setMessage("The login ID or password provided is incorrect");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(loginResponse.getMessage());
        }
    }

    @Override
    public ResponseEntity<String> logoutUser(String xXsrfToken) {
        AuthResponseDto logoutResponse = iamService.logoutUser();

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
        AuthResponseDto refreshResponse = AuthResponseDto.builder().build();
        refreshResponse.setMessage("Refresh token error");

        // check for cookies because endpoint doesn't need authorization
        if(cookies == null) {
            log.error("No cookie passed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(refreshResponse.getMessage());
        }

        // find for the refresh token cookie
        Cookie refreshTokenCookie = IamServiceUtility.getRefreshTokenCookie(request);

        // NO REFRESH TOKEN
        if(refreshTokenCookie == null) {
            log.error("No refresh token cookie");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(refreshResponse.getMessage());
        }

        // refresh the access token
        try {
            refreshResponse = iamService.refreshToken(refreshTokenCookie);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshResponse.getTokenCookie().toString())
                    .body(refreshResponse.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(refreshResponse.getMessage());
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
    public ResponseEntity<UserView> getUserDetails(Boolean includeId) {
        Optional<HttpServletRequest> requestOptional = getRequest();
        log.info("get user details");

        if(requestOptional.isEmpty())
            return ResponseEntity.internalServerError().body(null);

        HttpServletRequest request = requestOptional.get();
        Cookie tokenCookie = IamServiceUtility.getAccessTokenCookie(request);

        if(tokenCookie == null) {
            log.error("No token cookie passed");
            return ResponseEntity.ok(null);
        }

        return ResponseEntity.ok(iamService.getUserDetails(tokenCookie.getValue(), includeId));
    }

    @Override
    public ResponseEntity<IamUserData> getIamUserDetails(String xInternalCall, String loginId) {
        try {
            return ResponseEntity.ok(iamService.getIamUserDetails(loginId));
        } catch (UsernameNotFoundException e) {
            log.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<Boolean> adminCheck() {
        Optional<HttpServletRequest> requestOptional = getRequest();

        // don't have http request
        if(requestOptional.isEmpty())
            return ResponseEntity.internalServerError().body(null);

        HttpServletRequest request = requestOptional.get();
        Cookie tokenCookie = IamServiceUtility.getAccessTokenCookie(request);

        String tokenCookieValue = "";
        if(tokenCookie != null)
            tokenCookieValue = tokenCookie.getValue();

        return ResponseEntity.ok(iamService.adminCheck(tokenCookieValue));
    }

    @Override
    public ResponseEntity<Void> issueCsrfToken() {
        String csrfToken = UUID.randomUUID().toString();

        ResponseCookie responseCookie = ResponseCookie.from(SecurityUtility.CSRF_TOKEN_COOKIE_KEY, csrfToken)
                .httpOnly(false)
                .sameSite("None")
                .secure(true)
                .path("/")
                .maxAge(60 * 15)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .build();
    }

    @Override
    public ResponseEntity<Boolean> checkIsUserAuthenticated() {
        Optional<HttpServletRequest> requestOptional = getRequest();

        // don't have http request
        if(requestOptional.isEmpty())
            return ResponseEntity.internalServerError().body(null);

        HttpServletRequest request = requestOptional.get();
        Cookie refreshTokenCookie = IamServiceUtility.getRefreshTokenCookie(request);

        return ResponseEntity.ok(refreshTokenCookie != null);
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
