package com.superyoudi.kdbservice.controller;

import com.superyoudi.kdbservice.service.KnowledgeGraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Sijayyx
 */
@RestController
@RequestMapping("/kdb")
class KnowledgeGraphController {

    @Autowired
    private KnowledgeGraphService knowledgeGraphService;

    @GetMapping
    public String hello() {
        return "hello";
    }


}
