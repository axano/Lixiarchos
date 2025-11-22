package com.web.Lixiarchos.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public String redirectToPersons() {
        return "redirect:/persons";
    }
}
