package com.web.Lixiarchos.controllers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IndexControllerTest {

    private final IndexController controller = new IndexController();

    @Test
    void redirectToPersons_returnsRedirect() {
        assertEquals("redirect:/persons", controller.redirectToPersons());
    }
}
