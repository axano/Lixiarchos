package com.web.Lixiarchos.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ui.Model;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class LogViewControllerTest {

    private LogViewController controller;
    private Model model;
    private HttpServletRequest request;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        controller = new LogViewController();
        model = mock(Model.class);
        request = mock(HttpServletRequest.class);
        when(request.getAttribute("cspNonce")).thenReturn("test-nonce");
    }

    private void setLogPath(Path path) throws Exception {
        Field field = LogViewController.class.getDeclaredField("logPath");
        field.setAccessible(true);
        field.set(controller, path);
    }

    @Test
    void viewLogs_fileDoesNotExist_returnsNotFoundMessage() throws Exception {
        setLogPath(tempDir.resolve("nonexistent.log"));

        String view = controller.viewLogs(model, request);

        verify(model).addAttribute("logText", "Log file not found.");
        assertEquals("system-logs", view);
    }

    @Test
    void viewLogs_fileWithFewerThan500Lines_returnsAllLines() throws Exception {
        Path logFile = tempDir.resolve("app.log");
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < 100; i++) lines.add("line " + i);
        Files.write(logFile, lines);
        setLogPath(logFile);

        controller.viewLogs(model, request);

        verify(model).addAttribute("logText", String.join("\n", lines));
    }

    @Test
    void viewLogs_fileWithMoreThan500Lines_returnsLast500() throws Exception {
        Path logFile = tempDir.resolve("app.log");
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < 600; i++) lines.add("line " + i);
        Files.write(logFile, lines);
        setLogPath(logFile);

        controller.viewLogs(model, request);

        String expected = String.join("\n", lines.subList(100, 600));
        verify(model).addAttribute("logText", expected);
    }

    @Test
    void viewLogs_setsPageTitleAndCspNonce() throws Exception {
        setLogPath(tempDir.resolve("nonexistent.log"));

        String view = controller.viewLogs(model, request);

        verify(model).addAttribute("cspNonce", "test-nonce");
        verify(model).addAttribute("pageTitle", "System Logs");
        assertEquals("system-logs", view);
    }
}
