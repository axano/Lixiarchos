package com.web.Lixiarchos.scheduledJobs;

import com.web.Lixiarchos.services.DatabaseBackupService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;

import static com.web.Lixiarchos.services.EmailService.sendTextEmail;

@Component
public class DatabaseBackup {

    private final DatabaseBackupService backupService;
    @Value("${app.backup.path}")
    private String backupDir;

    public DatabaseBackup(DatabaseBackupService backupService) {
        this.backupService = backupService;
    }

    @Scheduled(cron = "0 15 3 * * *")
    public void scheduledBackup() {

        try {
            DatabaseBackupService.BackupResult backup = backupService.createBackup();
            File out = new File(
                    backupDir,
                    backup.filename
            );
            System.out.println("Saving backup to: " + out.getAbsolutePath());

            try (FileOutputStream fos = new FileOutputStream(out)) {
                fos.write(backup.data);
            }

            System.out.println("Backup saved: " + out.getAbsolutePath());
            sendTextEmail("perselis.e@gmail.com",
                    "Database Backup Created",
                    "A new database backup has been created: " + backup.filename);
            deleteOldBackups();

        } catch (Exception e) {
            System.err.println("Backup failed: " + e.getMessage());
        }
    }


    private void deleteOldBackups() {
        File dir = new File(backupDir);

        File[] files = dir.listFiles((d, name) ->
                name.startsWith("backup_") && name.endsWith(".sql"));

        if (files == null || files.length <= 10) return;

        Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));

        for (int i = 10; i < files.length; i++) {
            File file = files[i];
            if (file.delete()) {
                System.out.println("Deleted old backup: " + file.getName());
            } else {
                System.err.println("Failed to delete: " + file.getName());
            }
        }
    }
}