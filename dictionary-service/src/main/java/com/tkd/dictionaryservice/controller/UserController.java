package com.tkd.dictionaryservice.controller;

import com.tkd.apis.UserV1Api;
import com.tkd.dictionaryservice.service.UserService;
import com.tkd.dictionaryservice.utility.IamServiceUtility;
import com.tkd.models.UserAccount;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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
public class UserController implements UserV1Api {
    private final UserService userService;

    @Override
    public ResponseEntity<UserAccount> getUserDetails() {
        Optional<HttpServletRequest> requestOptional = getRequest();

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
            return ResponseEntity.ok(userService.getUserDetails(tokenCookie.getValue()));
        } catch (ExpiredJwtException e) {
            log.error("Token expired getting user details");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (UsernameNotFoundException e) {
            log.error(e.getMessage());
            log.error("Invalid refresh token getting user details");
            return ResponseEntity.internalServerError().body(null);
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
