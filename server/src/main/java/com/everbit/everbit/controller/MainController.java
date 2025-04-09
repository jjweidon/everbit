package com.everbit.everbit.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MainController {

    @GetMapping("/")
    public ResponseEntity<?> hello() {
        log.info("HELLO EVERBIT");
        try {
            return ResponseEntity.ok().body("HELLO EVERBIT");
        } catch (Exception e) {
            log.error("HELLO ERROR: ", e);
            return ResponseEntity.badRequest().body("HELLO ERROR🥲");
        }
    }
}
