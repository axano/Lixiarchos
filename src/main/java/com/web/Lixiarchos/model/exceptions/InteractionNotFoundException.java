package com.web.Lixiarchos.model.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class InteractionNotFoundException extends RuntimeException {

    public InteractionNotFoundException() {

        super("Interaction not found");
    }
}
