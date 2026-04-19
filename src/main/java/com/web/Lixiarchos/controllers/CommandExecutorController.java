package com.web.Lixiarchos.controllers;

import com.web.Lixiarchos.model.Person;
import com.web.Lixiarchos.repositories.PersonRepository;
import com.web.Lixiarchos.services.CommandExecutionService;
import com.web.Lixiarchos.services.CommandExecutionService.CommandExecutionResult;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;
import java.util.UUID;

/**
 * Controller for executing server commands on selected persons.
 * Restricted to ADMIN role only.
 */
@Controller
@RequestMapping("/admin/osint")
@PreAuthorize("hasRole('ADMIN')")
public class CommandExecutorController {

    private static final Logger logger = LoggerFactory.getLogger(CommandExecutorController.class);

    private final PersonRepository personRepository;
    private final CommandExecutionService commandExecutionService;

    public CommandExecutorController(PersonRepository personRepository,
                                     CommandExecutionService commandExecutionService) {
        this.personRepository = personRepository;
        this.commandExecutionService = commandExecutionService;
    }

    /**
     * Displays the command executor page with a list of all persons.
     * Admins can select a person and execute commands on their usernames.
     */
    @GetMapping("")
    public String showCommandExecutor(Model model, HttpServletRequest request) {
        try {
            model.addAttribute("persons", personRepository.findAll());
            model.addAttribute("cspNonce", generateCspNonce(request));
            model.addAttribute("pageTitle", "Command Executor");
            return "osint";
        } catch (Exception e) {
            logger.error("Error loading command executor page", e);
            model.addAttribute("errorMessage", "Error loading persons: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Executes the "sherlock" command on the usernames of a selected person.
     * The sherlock command is executed with all usernames as arguments separated by space.
     *
     * @param personId the ID of the person whose usernames to use
     * @param model the Spring model
     * @param request the HTTP request
     * @return the command executor view with results
     */
    @PostMapping("/execute-sherlock")
    public String executeSherlock(@RequestParam Integer personId,
                                  Model model,
                                  HttpServletRequest request) {
        try {
            // Fetch the person
            Person person = personRepository.findById(personId)
                    .orElseThrow(() -> new IllegalArgumentException("Person not found with ID: " + personId));

            logger.info("Executing sherlock command for person: {} (ID: {})", person.getName(), personId);

            // Get usernames from person
            String usernamesString = person.getUsernamesString();
            if (usernamesString == null || usernamesString.trim().isEmpty()) {
                logger.warn("Person {} has no usernames", personId);
                model.addAttribute("errorMessage", "Selected person has no usernames configured.");
                model.addAttribute("persons", personRepository.findAll());
                model.addAttribute("cspNonce", generateCspNonce(request));
                return "osint";
            }

            // Parse usernames
            String[] usernames = usernamesString.split(",");
            // Trim all usernames to remove whitespace
            for (int i = 0; i < usernames.length; i++) {
                usernames[i] = usernames[i].trim();
            }
             logger.info("Executing sherlock with {} usernames", usernames.length);

             // Execute sherlock command
             CommandExecutionResult result = commandExecutionService.executeCommandWithArray("sherlock", usernames);

             // Prepare model for response
            model.addAttribute("persons", personRepository.findAll());
            model.addAttribute("selectedPerson", person);
            model.addAttribute("commandResult", result);
            model.addAttribute("commandName", "sherlock");
            model.addAttribute("usernamesList", usernames);  // Pass as array for Thymeleaf iteration
            model.addAttribute("cspNonce", generateCspNonce(request));
            model.addAttribute("pageTitle", "Command Executor");

            if (result.isSuccess()) {
                logger.info("Sherlock command executed successfully");
                model.addAttribute("successMessage", "Command executed successfully!");
            } else {
                logger.error("Sherlock command failed with exit code: {}", result.getExitCode());
                model.addAttribute("errorMessage", "Command failed with exit code: " + result.getExitCode());
            }

            return "osint";

        } catch (IllegalArgumentException e) {
            logger.error("Invalid person ID", e);
            model.addAttribute("errorMessage", "Invalid person selected: " + e.getMessage());
            model.addAttribute("persons", personRepository.findAll());
            model.addAttribute("cspNonce", generateCspNonce(request));
            return "osint";
        } catch (Exception e) {
            logger.error("Error executing sherlock command", e);
            model.addAttribute("errorMessage", "Error executing command: " + e.getMessage());
            model.addAttribute("persons", personRepository.findAll());
            model.addAttribute("cspNonce", generateCspNonce(request));
            return "osint";
        }
    }

    /**
     * REST API endpoint for asynchronous command execution.
     * Returns JSON response with command results.
     *
     * @param personId the ID of the person whose usernames to use
     * @return JSON response with command results
     */
    @PostMapping("/api/execute-sherlock")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> executeSherlockAsync(@RequestParam Integer personId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Fetch the person
            Person person = personRepository.findById(personId)
                    .orElseThrow(() -> new IllegalArgumentException("Person not found with ID: " + personId));

            logger.info("Executing sherlock command for person: {} (ID: {})", person.getName(), personId);

            // Get usernames from person
            String usernamesString = person.getUsernamesString();
            if (usernamesString == null || usernamesString.trim().isEmpty()) {
                logger.warn("Person {} has no usernames", personId);
                response.put("success", false);
                response.put("message", "Selected person has no usernames configured.");
                return ResponseEntity.ok(response);
            }

            // Parse usernames
            String[] usernames = usernamesString.split(",");
            // Trim all usernames to remove whitespace
            for (int i = 0; i < usernames.length; i++) {
                usernames[i] = usernames[i].trim();
            }
             logger.info("Executing sherlock with {} usernames", usernames.length);

             // Execute sherlock command
             CommandExecutionResult result = commandExecutionService.executeCommandWithArray("sherlock", usernames);

             // Prepare response
             response.put("success", result.isSuccess());
            response.put("exitCode", result.getExitCode());
            response.put("stdout", result.getStdout());
            response.put("stderr", result.getStderr());
            response.put("usernames", usernames);
            response.put("personName", person.getName() + " " + person.getSurname());

            if (result.isSuccess()) {
                logger.info("Sherlock command executed successfully");
                response.put("message", "Command executed successfully!");
            } else {
                logger.error("Sherlock command failed with exit code: {}", result.getExitCode());
                response.put("message", "Command failed with exit code: " + result.getExitCode());
            }

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid person ID", e);
            response.put("success", false);
            response.put("message", "Invalid person selected: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Error executing sherlock command", e);
            response.put("success", false);
            response.put("message", "Error executing command: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Generates or retrieves the CSP nonce for inline scripts.
     *
     * @param request the HTTP request
     * @return the nonce value
     */
    private String generateCspNonce(HttpServletRequest request) {
        String nonce = (String) request.getAttribute("cspNonce");
        if (nonce == null) {
            nonce = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
            request.setAttribute("cspNonce", nonce);
        }
        return nonce;
    }
}

