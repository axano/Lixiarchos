package com.web.Lixiarchos.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPanelController {

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public String adminPanel(Model model) {
        model.addAttribute("pageTitle", "Admin Panel");
        return "admin-panel";
    }
}
