package com.example.PartTimeHR.common.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/")
    public String home() {
        return "PartTimeHR Server Running";
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}