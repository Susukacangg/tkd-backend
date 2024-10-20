package com.tkd.dictionaryservice.controller;

import com.tkd.apis.DictV1Api;
import com.tkd.dictionaryservice.service.DictionaryService;
import com.tkd.models.Word;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class DictionaryController implements DictV1Api {
    private final DictionaryService dictionaryService;

    @Override
    public ResponseEntity<String> addToDictionary(Word newWord) {
        HttpServletRequest request = getRequest().orElseThrow(() -> new RuntimeException("request is null"));

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            log.error("No cookies passed adding new word");
            return ResponseEntity.internalServerError().body(null);
        }

        Cookie tokenCookie = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("token"))
                tokenCookie = cookie;
        }

        if (tokenCookie == null) {
            log.error("No token cookie passed adding new word");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        log.info(newWord.toString());
        ResponseCookie responseCookie = ResponseCookie.from(tokenCookie.getName(), tokenCookie.getValue())
                .httpOnly(tokenCookie.isHttpOnly())
                .sameSite("None")
                .secure(tokenCookie.getSecure())
                .path(tokenCookie.getPath())
                .maxAge(tokenCookie.getMaxAge())
                .build();
        dictionaryService.addNewWord(newWord, responseCookie.toString());
        return ResponseEntity.ok().body("success");
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
