package com.web.Lixiarchos.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DatabaseBackupServiceTest {

    private DatabaseBackupService service;

    @BeforeEach
    void setUp() {
        service = new DatabaseBackupService();
    }

    private String callExtractDatabaseName(String url) throws Exception {
        Method method = DatabaseBackupService.class.getDeclaredMethod("extractDatabaseName", String.class);
        method.setAccessible(true);
        return (String) method.invoke(service, url);
    }

    @Test
    void extractDatabaseName_urlWithoutQueryString_returnsDbName() throws Exception {
        assertEquals("mydb", callExtractDatabaseName("jdbc:mysql://localhost:3306/mydb"));
    }

    @Test
    void extractDatabaseName_urlWithQueryString_stripsQueryParams() throws Exception {
        assertEquals("mydb", callExtractDatabaseName("jdbc:mysql://localhost:3306/mydb?useSSL=false&serverTimezone=UTC"));
    }

    @Test
    void extractDatabaseName_urlWithSchemaName_returnsCorrectName() throws Exception {
        assertEquals("lixiarchos", callExtractDatabaseName("jdbc:mysql://localhost:3306/lixiarchos?useSSL=true&serverTimezone=UTC"));
    }
}
