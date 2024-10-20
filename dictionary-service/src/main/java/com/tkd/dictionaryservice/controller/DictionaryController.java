package com.tkd.dictionaryservice.controller;

import com.tkd.apis.DictV1Api;
import com.tkd.models.NewWordRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class DictionaryController implements DictV1Api {
    @Override
    public ResponseEntity<String> addToDictionary(NewWordRequest body) {
        log.info(body.toString());
        return ResponseEntity.ok().body("success");
    }

    @Override
    public Optional<HttpServletRequest> getRequest() {
        return DictV1Api.super.getRequest();
    }
}
