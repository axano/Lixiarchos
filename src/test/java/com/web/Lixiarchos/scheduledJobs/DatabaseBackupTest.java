package com.web.Lixiarchos.scheduledJobs;

import com.web.Lixiarchos.services.DatabaseBackupService;
import com.web.Lixiarchos.services.EmailService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import org.mockito.ArgumentMatchers;

import static org.mockito.Mockito.*;

class DatabaseBackupTest {

    @TempDir
    Path tempDir;

    @Mock private DatabaseBackupService backupService;
    @Mock private JavaMailSender mailSender;

    @InjectMocks
    private DatabaseBackup databaseBackup;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        injectStaticMailSender(mailSender);
        setField("backupDir", tempDir.toString());
        setField("notificationEmail", "notify@example.com");
    }

    @AfterEach
    void tearDown() throws Exception {
        injectStaticMailSender(null);
    }

    private void injectStaticMailSender(JavaMailSender sender) throws Exception {
        Field field = EmailService.class.getDeclaredField("staticMailSender");
        field.setAccessible(true);
        field.set(null, sender);
    }

    private void setField(String name, Object value) throws Exception {
        Field field = DatabaseBackup.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(databaseBackup, value);
    }

    private DatabaseBackupService.BackupResult validResult() {
        return new DatabaseBackupService.BackupResult(
                "backup_db_20240101_120000.sql",
                "-- SQL dump".getBytes()
        );
    }

    @Test
    void scheduledBackup_success_writesFileAndSendsEmail() throws Exception {
        when(backupService.createBackup()).thenReturn(validResult());

        databaseBackup.scheduledBackup();

        assertTrue(tempDir.resolve("backup_db_20240101_120000.sql").toFile().exists());
        verify(mailSender).send(ArgumentMatchers.<org.springframework.mail.SimpleMailMessage>any());
    }

    @Test
    void scheduledBackup_success_writesCorrectFileContents() throws Exception {
        byte[] data = "-- SQL dump content".getBytes();
        DatabaseBackupService.BackupResult result = new DatabaseBackupService.BackupResult(
                "backup_db_20240101_120000.sql", data
        );
        when(backupService.createBackup()).thenReturn(result);

        databaseBackup.scheduledBackup();

        byte[] written = Files.readAllBytes(tempDir.resolve("backup_db_20240101_120000.sql"));
        assertArrayEquals(data, written);
    }

    @Test
    void scheduledBackup_serviceThrows_noFileWrittenNoEmailSent() throws Exception {
        when(backupService.createBackup()).thenThrow(new RuntimeException("mysqldump failed"));

        assertDoesNotThrow(() -> databaseBackup.scheduledBackup());

        File[] files = tempDir.toFile().listFiles();
        assertEquals(0, files == null ? 0 : files.length);
        verify(mailSender, never()).send(ArgumentMatchers.<org.springframework.mail.SimpleMailMessage>any());
    }

    @Test
    void scheduledBackup_prunesOldBackupsWhenMoreThan10Exist() throws Exception {
        // Create 11 pre-existing backup files
        for (int i = 0; i < 11; i++) {
            Files.writeString(tempDir.resolve("backup_old_" + String.format("%02d", i) + ".sql"), "old");
        }
        // scheduledBackup writes 1 more → 12 total → pruner removes 2 → 10 remain
        when(backupService.createBackup()).thenReturn(validResult());

        databaseBackup.scheduledBackup();

        File[] remaining = tempDir.toFile().listFiles(
                (dir, name) -> name.startsWith("backup_") && name.endsWith(".sql")
        );
        assertNotNull(remaining);
        assertEquals(10, remaining.length);
    }

    @Test
    void scheduledBackup_exactlyTenBackups_noneDeleted() throws Exception {
        // Create 9 pre-existing backup files; scheduledBackup adds 1 → exactly 10 → none deleted
        for (int i = 0; i < 9; i++) {
            Files.writeString(tempDir.resolve("backup_old_" + i + ".sql"), "old");
        }
        when(backupService.createBackup()).thenReturn(validResult());

        databaseBackup.scheduledBackup();

        File[] remaining = tempDir.toFile().listFiles(
                (dir, name) -> name.startsWith("backup_") && name.endsWith(".sql")
        );
        assertNotNull(remaining);
        assertEquals(10, remaining.length);
    }
}
