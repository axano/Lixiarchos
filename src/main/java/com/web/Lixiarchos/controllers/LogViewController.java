package com.web.Lixiarchos.controllers;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Controller
public class LogViewController {

    @Value("${logging.file.name}")
    private String pathToLogFile;

    private Path logPath;

    @PostConstruct
    public void init() {
        logPath = Path.of(pathToLogFile);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/logs")
    public String viewLogs(Model model, HttpServletRequest request) throws IOException {
        List<String> lines = List.of("Log file not found.");

        if (Files.exists(logPath)) {
            List<String> allLines = Files.readAllLines(logPath);
            int start = Math.max(0, allLines.size() - 500);
            lines = allLines.subList(start, allLines.size());
        }

        // Get the nonce generated in the filter
        String cspNonce = (String) request.getAttribute("cspNonce");

        model.addAttribute("cspNonce", cspNonce);
        model.addAttribute("pageTitle", "System Logs");
        model.addAttribute("logText", String.join("\n", lines));

        return "system-logs";
    }
}
