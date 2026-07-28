package com.superyoudi.businessservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Sijayyx
 */
@RestController
@RequestMapping
class TestController {

    @GetMapping("/test")
    public String test() {
        return "hello world!";
    }
}
