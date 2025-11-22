package com.web.Lixiarchos.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DatabaseBackupService {

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPass;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    public static class BackupResult {
        public final String filename;
        public final byte[] data;

        public BackupResult(String filename, byte[] data) {
            this.filename = filename;
            this.data = data;
        }
    }

    public BackupResult createBackup() throws Exception {
        String databaseName = extractDatabaseName(dbUrl);
        String mysqldumpCmd = findMysqldumpExecutable();

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        String filename = "backup_" + databaseName + "_" + timestamp + ".sql";

        ProcessBuilder pb = new ProcessBuilder(
                mysqldumpCmd,
                "-u" + dbUser,
                "-p" + dbPass,
                "--databases",
                databaseName
        );

        Process process = pb.start();
        String sqlDump = readInputStream(process.getInputStream());
        String errors = readInputStream(process.getErrorStream());

        int exit = process.waitFor();

        if (exit != 0) {
            throw new RuntimeException("mysqldump failed:\n" + errors);
        }

        return new BackupResult(filename, sqlDump.getBytes(StandardCharsets.UTF_8));
    }

    private String extractDatabaseName(String url) {
        String afterSlash = url.substring(url.lastIndexOf("/") + 1);
        return afterSlash.contains("?") ? afterSlash.substring(0, afterSlash.indexOf("?")) : afterSlash;
    }

    private String readInputStream(InputStream inputStream) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }

    private String findMysqldumpExecutable() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            String[] paths = {
                    "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe",
                    "C:\\Program Files\\MySQL\\MySQL Server 5.7\\bin\\mysqldump.exe",
                    "C:\\Program Files (x86)\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe",
                    "C:\\xampp\\mysql\\bin\\mysqldump.exe",
                    "C:\\wamp64\\bin\\mysql\\mysql8.0.0\\bin\\mysqldump.exe"
            };
            for (String p : paths) {
                if (new File(p).exists()) return p;
            }
            return "mysqldump.exe"; // fallback to PATH
        }

        // Linux / macOS: assume mysqldump is in PATH
        return "mysqldump";
    }

    private boolean isLinux(String cmd) {
        return !System.getProperty("os.name").toLowerCase().contains("win") && cmd.equals("mysqldump");
    }
}
