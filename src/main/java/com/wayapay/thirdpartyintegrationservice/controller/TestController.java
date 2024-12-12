package com.wayapay.thirdpartyintegrationservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.wayapay.thirdpartyintegrationservice.v2.dto.Constants.API_V1;

@RestController
@RequestMapping("/testing")
@Slf4j
@RequiredArgsConstructor
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "test";
    }
}
