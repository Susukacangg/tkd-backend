package com.tkd.dictionaryservice.controller;

import com.tkd.apis.DictV1Api;
import com.tkd.dictionaryservice.service.DictionaryService;
import com.tkd.dictionaryservice.utility.DictionaryServiceUtility;
import com.tkd.models.ReportRequest;
import com.tkd.models.WordModel;
import feign.FeignException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class DictionaryController implements DictV1Api {
    private final DictionaryService dictionaryService;

    @Override
    public ResponseEntity<BigDecimal> addToDictionary(String xXsrfToken, WordModel newWord) {
        HttpServletRequest request = getRequest().orElseThrow(() -> new RuntimeException("request is null"));

        Cookie tokenCookie = DictionaryServiceUtility.getAccessTokenCookie(request);

        if (tokenCookie == null) {
            log.error("No token cookie passed adding new word");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ResponseCookie responseCookie = ResponseCookie.from(tokenCookie.getName(), tokenCookie.getValue())
                .httpOnly(tokenCookie.isHttpOnly())
                .sameSite("Lax")
                .secure(tokenCookie.getSecure())
                .path(tokenCookie.getPath())
                .maxAge(tokenCookie.getMaxAge())
                .build();

        return ResponseEntity.ok(dictionaryService.addNewWord(newWord, responseCookie.toString()));
    }

    @Override
    public ResponseEntity<List<WordModel>> getRandomWords() {
        return ResponseEntity.ok(dictionaryService.getRandomWords());
    }

    @Override
    public ResponseEntity<WordModel> getWord(BigDecimal wordId) {
        WordModel dictionaryItem = dictionaryService.getWord(wordId);
        if (dictionaryItem == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(dictionaryItem);
    }

    @Override
    public ResponseEntity<Integer> editWord(String xXsrfToken, WordModel body) {
        return ResponseEntity.ok(dictionaryService.editWord(body));
    }

    @Override
    public ResponseEntity<Object> findWord(String word, Integer pageNum) {
        return ResponseEntity.ok(dictionaryService.findWord(word, pageNum));
    }

    @Override
    public ResponseEntity<List<String>> suggestWord(String searchStr) {
        return ResponseEntity.ok(dictionaryService.suggestWord(searchStr));
    }

    @Override
    public ResponseEntity<Object> getAllUserWords(Integer pageNum) {
        HttpServletRequest request = getRequest().orElseThrow(() -> new RuntimeException("request is null"));

        Cookie tokenCookie = DictionaryServiceUtility.getAccessTokenCookie(request);

        if (tokenCookie == null) {
            log.error("No token cookie passed adding new word");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ResponseCookie responseCookie = ResponseCookie.from(tokenCookie.getName(), tokenCookie.getValue())
                .httpOnly(tokenCookie.isHttpOnly())
                .sameSite("Lax")
                .secure(tokenCookie.getSecure())
                .path(tokenCookie.getPath())
                .maxAge(tokenCookie.getMaxAge())
                .build();

        try {
            Page<WordModel> dictionaryItems = dictionaryService.getAllUserWords(responseCookie.toString(), pageNum);
            return ResponseEntity.ok(dictionaryItems);
        } catch (FeignException.Forbidden error) {
            log.info(error.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Override
    public ResponseEntity<Void> deleteWord(BigDecimal wordId, String xXsrfToken) {
        if(dictionaryService.deleteWord(wordId))
            return ResponseEntity.ok().build();

        return ResponseEntity.internalServerError().build();
    }

    @Override
    public ResponseEntity<Void> reportContribution(String xXsrfToken, ReportRequest body) {
        HttpServletRequest request = getRequest().orElseThrow(() -> new RuntimeException("request is null"));

        Cookie tokenCookie = DictionaryServiceUtility.getAccessTokenCookie(request);

        if (tokenCookie == null) {
            log.error("No token cookie passed adding new word");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ResponseCookie responseCookie = ResponseCookie.from(tokenCookie.getName(), tokenCookie.getValue())
                .httpOnly(tokenCookie.isHttpOnly())
                .sameSite("Lax")
                .secure(tokenCookie.getSecure())
                .path(tokenCookie.getPath())
                .maxAge(tokenCookie.getMaxAge())
                .build();

        dictionaryService.reportContribution(body, responseCookie.toString());
        return ResponseEntity.ok().build();
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
