package com.web.Lixiarchos.controllers;

import com.web.Lixiarchos.services.DatabaseBackupService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BackupWebController {

    private final DatabaseBackupService backupService;

    public BackupWebController(DatabaseBackupService backupService) {
        this.backupService = backupService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/backup")
    public ResponseEntity<byte[]> backupDatabase() {
        try {
            DatabaseBackupService.BackupResult result = backupService.createBackup();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + result.filename)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(result.data);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(("Backup failed: " + e.getMessage()).getBytes());
        }
    }
}
