package com.tkd.dictionaryservice.controller;

import com.tkd.apis.UserV1Api;
import com.tkd.dictionaryservice.service.IamService;
import com.tkd.dictionaryservice.service.UserService;
import com.tkd.dictionaryservice.utility.IamServiceUtility;
import com.tkd.models.UserAccount;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
        if(requestOptional.isPresent()) {
            HttpServletRequest request = requestOptional.get();

            Cookie[] cookies = request.getCookies();
            String token = null;
            if(cookies != null) {
                for(Cookie cookie : cookies)
                    if(cookie.getName().equals(IamServiceUtility.TOKEN_COOKIE_KEY))
                        token = cookie.getValue();
                return ResponseEntity.ok(userService.getUserDetails(token));
            }
        }

        return ResponseEntity.internalServerError().body(null);
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
