package com.web.Lixiarchos.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for executing server commands with security considerations.
 * This service handles command execution and output capture.
 */
@Service
public class CommandExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(CommandExecutionService.class);

    /**
     * Executes a command on the server with the given arguments.
     * Currently supports: sherlock command
     *
     * @param command the command to execute (e.g., "sherlock")
     * @param arguments variable number of arguments to pass to the command
     * @return CommandExecutionResult containing exit code, stdout, and stderr
     */
    public CommandExecutionResult executeCommand(String command, String... arguments) {
        try {
            // Validate command
            if (command == null || command.trim().isEmpty()) {
                logger.error("Command cannot be null or empty");
                return CommandExecutionResult.error("Command cannot be null or empty");
            }

            // Only allow whitelisted commands for security
            if (!isCommandAllowed(command)) {
                logger.error("Command not allowed: {}", command);
                return CommandExecutionResult.error("Command '" + command + "' is not allowed");
            }

            // Flatten arguments - handle case where a String[] is passed as single varargs argument
            List<String> flattenedArgs = new ArrayList<>();
            if (arguments != null && arguments.length > 0) {
                // Check if we received a single String[] array (happens when called with executeCommand("cmd", stringArray))
                if (arguments.length == 1 && arguments[0] != null) {
                    // Try to detect if this is actually an array that was incorrectly passed
                    // This is a bit of a hack but necessary due to Java varargs behavior
                    String firstArg = arguments[0];
                    if (firstArg.contains("[L")) {
                        // This looks like a reference to an array object, not a regular string
                        // This shouldn't happen with proper varargs passing
                        logger.warn("Detected potential varargs array expansion issue");
                    }
                }
                
                for (String arg : arguments) {
                    if (arg != null && !arg.trim().isEmpty()) {
                        flattenedArgs.add(arg.trim());
                    }
                }
            }

            // Validate arguments
            for (String arg : flattenedArgs) {
                if (!isArgumentSafe(arg)) {
                    logger.error("Unsafe argument detected: {}", arg);
                    return CommandExecutionResult.error("Unsafe argument detected: " + arg);
                }
            }

            // Build command list
            List<String> commandList = new ArrayList<>();
            commandList.add(command);
            
            // Add sherlock-specific flags if this is a sherlock command
            if ("sherlock".equalsIgnoreCase(command)) {
                commandList.add("--nsfw");
                commandList.add("--no-txt");
                logger.info("Added sherlock flags: --nsfw --no-txt");
            }
            
            // Add all arguments
            commandList.addAll(flattenedArgs);

            logger.info("Executing command: {} with {} arguments", command, flattenedArgs.size());
            logger.info("Full command line: {}", String.join(" ", commandList));

            // Execute command
            ProcessBuilder processBuilder = new ProcessBuilder(commandList);
            processBuilder.redirectErrorStream(false);
            Process process = processBuilder.start();

            // Capture output
            String stdout = captureOutput(process.getInputStream());
            String stderr = captureOutput(process.getErrorStream());

            // Wait for completion
            int exitCode = process.waitFor();

            logger.info("Command completed with exit code: {}", exitCode);

            return new CommandExecutionResult(exitCode, stdout, stderr);

        } catch (java.io.IOException e) {
            logger.error("Failed to start process: {}", command, e);
            String errorMsg = "Command '" + command + "' could not be executed. ";
            String osName = System.getProperty("os.name").toLowerCase();
            
            if (osName.contains("win")) {
                errorMsg += "Ensure the command is installed and available in your system PATH. " +
                        "For sherlock: verify with 'sherlock --version' or reinstall with 'pip install sherlock-project'";
            } else {
                errorMsg += "Ensure the command is installed and available in your system PATH. " +
                        "For sherlock: verify with 'sherlock --version' or reinstall with 'pip install sherlock-project'";
            }
            return CommandExecutionResult.error(errorMsg);
        } catch (InterruptedException e) {
            logger.error("Command execution was interrupted: {}", command, e);
            Thread.currentThread().interrupt();
            return CommandExecutionResult.error("Command execution was interrupted: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error executing command: {}", command, e);
            return CommandExecutionResult.error("Error executing command: " + e.getMessage());
        }
    }

    /**
     * Overloaded method to execute a command with a String array of arguments.
     * This method properly handles arrays without varargs confusion.
     *
     * @param command the command to execute (e.g., "sherlock")
     * @param argumentsArray array of arguments to pass to the command
     * @return CommandExecutionResult containing exit code, stdout, and stderr
     */
    public CommandExecutionResult executeCommandWithArray(String command, String[] argumentsArray) {
        // Get the individual arguments and call the varargs version properly
        if (argumentsArray == null || argumentsArray.length == 0) {
            return executeCommand(command);
        }
        
        // The trick: call executeCommand with each argument separately (varargs expansion)
        // We can't do this directly in one call due to varargs, so we build the command here
        try {
            // Validate command
            if (command == null || command.trim().isEmpty()) {
                logger.error("Command cannot be null or empty");
                return CommandExecutionResult.error("Command cannot be null or empty");
            }

            // Only allow whitelisted commands for security
            if (!isCommandAllowed(command)) {
                logger.error("Command not allowed: {}", command);
                return CommandExecutionResult.error("Command '" + command + "' is not allowed");
            }

            // Validate each argument
            for (String arg : argumentsArray) {
                if (arg != null && !arg.trim().isEmpty()) {
                    if (!isArgumentSafe(arg)) {
                        logger.error("Unsafe argument detected: {}", arg);
                        return CommandExecutionResult.error("Unsafe argument detected: " + arg);
                    }
                }
            }

            // Build command list
            List<String> commandList = new ArrayList<>();
            commandList.add(command);
            
            // Add sherlock-specific flags if this is a sherlock command
            if ("sherlock".equalsIgnoreCase(command)) {
                commandList.add("--nsfw");
                commandList.add("--no-txt");
                logger.info("Added sherlock flags: --nsfw --no-txt");
            }
            
            for (String arg : argumentsArray) {
                if (arg != null && !arg.trim().isEmpty()) {
                    commandList.add(arg.trim());
                }
            }

            logger.info("Executing command: {} with {} arguments", command, argumentsArray.length);
            logger.info("Full command line: {}", String.join(" ", commandList));

            // Execute command
            ProcessBuilder processBuilder = new ProcessBuilder(commandList);
            processBuilder.redirectErrorStream(false);
            Process process = processBuilder.start();

            // Capture output
            String stdout = captureOutput(process.getInputStream());
            String stderr = captureOutput(process.getErrorStream());

            // Wait for completion
            int exitCode = process.waitFor();

            logger.info("Command completed with exit code: {}", exitCode);

            return new CommandExecutionResult(exitCode, stdout, stderr);

        } catch (java.io.IOException e) {
            logger.error("Failed to start process: {}", command, e);
            String errorMsg = "Command '" + command + "' could not be executed. ";
            String osName = System.getProperty("os.name").toLowerCase();
            
            if (osName.contains("win")) {
                errorMsg += "Ensure the command is installed and available in your system PATH. " +
                        "For sherlock: verify with 'sherlock --version' or reinstall with 'pip install sherlock-project'";
            } else {
                errorMsg += "Ensure the command is installed and available in your system PATH. " +
                        "For sherlock: verify with 'sherlock --version' or reinstall with 'pip install sherlock-project'";
            }
            return CommandExecutionResult.error(errorMsg);
        } catch (InterruptedException e) {
            logger.error("Command execution was interrupted: {}", command, e);
            Thread.currentThread().interrupt();
            return CommandExecutionResult.error("Command execution was interrupted: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error executing command: {}", command, e);
            return CommandExecutionResult.error("Error executing command: " + e.getMessage());
        }
    }


    /**
     * Checks if the command is in the whitelist of allowed commands.
     *
     * @param command the command to check
     * @return true if command is allowed, false otherwise
     */
    private boolean isCommandAllowed(String command) {
        // Whitelist of allowed commands
        String[] allowedCommands = {"sherlock"};
        for (String allowed : allowedCommands) {
            if (command.equalsIgnoreCase(allowed)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates that an argument is safe to use.
     * Prevents command injection attacks.
     *
     * @param argument the argument to validate
     * @return true if argument is safe, false otherwise
     */
    private boolean isArgumentSafe(String argument) {
        // Check for dangerous patterns
        String[] dangerousPatterns = {";", "|", "&", "`", "$", "(", ")", "<", ">", "\n", "\r"};
        for (String pattern : dangerousPatterns) {
            if (argument.contains(pattern)) {
                return false;
            }
        }
        // Only allow alphanumeric, underscore, hyphen, and dot
        return argument.matches("^[a-zA-Z0-9_.-]+$");
    }

    /**
     * Captures the output from an InputStream.
     *
     * @param inputStream the stream to read from
     * @return the captured output as a string
     */
    private String captureOutput(java.io.InputStream inputStream) {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        } catch (Exception e) {
            logger.error("Error capturing output", e);
        }
        return output.toString();
    }

    /**
     * Inner class to hold command execution results.
     */
    public static class CommandExecutionResult {
        private final int exitCode;
        private final String stdout;
        private final String stderr;
        private final boolean success;

        public CommandExecutionResult(int exitCode, String stdout, String stderr) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
            this.success = exitCode == 0;
        }

        private CommandExecutionResult(String errorMessage) {
            this.exitCode = -1;
            this.stdout = "";
            this.stderr = errorMessage;
            this.success = false;
        }

        public static CommandExecutionResult error(String errorMessage) {
            return new CommandExecutionResult(errorMessage);
        }

        public int getExitCode() { return exitCode; }
        public String getStdout() { return stdout; }
        public String getStderr() { return stderr; }
        public boolean isSuccess() { return success; }
    }
}
