package com.web.Lixiarchos.controllers;

import com.web.Lixiarchos.services.DatabaseBackupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BackupWebControllerTest {

    @Mock
    private DatabaseBackupService backupService;

    @InjectMocks
    private BackupWebController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void backupDatabase_success_returnsFileDownload() throws Exception {
        byte[] sqlData = "-- SQL dump content".getBytes();
        DatabaseBackupService.BackupResult result =
                new DatabaseBackupService.BackupResult("backup_db_20240101_120000.sql", sqlData);
        when(backupService.createBackup()).thenReturn(result);

        ResponseEntity<byte[]> response = controller.backupDatabase();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(
                "attachment; filename=backup_db_20240101_120000.sql",
                response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)
        );
        assertArrayEquals(sqlData, response.getBody());
    }

    @Test
    void backupDatabase_serviceFails_returnsInternalServerError() throws Exception {
        when(backupService.createBackup()).thenThrow(new RuntimeException("mysqldump failed"));

        ResponseEntity<byte[]> response = controller.backupDatabase();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Backup failed: mysqldump failed", new String(response.getBody()));
    }

    @Test
    void backupDatabase_serviceFailsWithNullMessage_bodyContainsNull() throws Exception {
        when(backupService.createBackup()).thenThrow(new RuntimeException((String) null));

        ResponseEntity<byte[]> response = controller.backupDatabase();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Backup failed: null", new String(response.getBody()));
    }
}
